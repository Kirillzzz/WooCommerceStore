package mx.niluxer.store.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mx.niluxer.store.R;
import mx.niluxer.store.data.model.Product;
import mx.niluxer.store.utils.CustomPicasso;
import mx.niluxer.store.utils.UnsafeOkHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Product> mItems;
    private Context mContext;
    private ProductItemListener mItemListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public TextView tvProductName, tvProductPrice;
        public ImageView ivProductImage;
        ProductItemListener mItemListener;

        public ViewHolder(View itemView, ProductItemListener postItemListener) {
            super(itemView);
            tvProductName  = (TextView) itemView.findViewById(R.id.tvProductName);
            tvProductPrice = (TextView) itemView.findViewById(R.id.tvProductPrice);
            ivProductImage = (ImageView) itemView.findViewById(R.id.ivProductImage);

            this.mItemListener = postItemListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Product item = getItem(getAdapterPosition());
            this.mItemListener.onProductClick(item.getId());

            notifyDataSetChanged();
        }

        @Override
        public boolean onLongClick(View v) {
            Product item = getItem(getAdapterPosition());
            this.mItemListener.onProductLongClick(item);

            //notifyDataSetChanged();
            return true;
        }
    }

    public ProductsAdapter(Context context, List<Product> posts, ProductItemListener itemListener) {
        mItems = posts;
        mContext = context;
        mItemListener = itemListener;
    }

    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View productView = inflater.inflate(R.layout.product_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(productView, this.mItemListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ProductsAdapter.ViewHolder holder, int position) {

        Product item = mItems.get(position);
        TextView textView1 = holder.tvProductName;
        TextView textView2 = holder.tvProductPrice;
        ImageView imageView = holder.ivProductImage;
        textView1.setText(item.getName());
        textView2.setText(item.getPrice());
        Picasso.get().load(item.getImages().get(0).getSrc().replace("https", "http")).transform(new CropCircleTransformation()).resize(150, 150).into(imageView);


        /*OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Picasso picasso = new Picasso.Builder(mContext)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        picasso.get().load("http://192.168.1.66/~niluxer/wordpress/wp-content/uploads/2018/05/long-sleeve-tee-2.jpg").into(holder.ivProductImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateProducts(List<Product> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    private Product getItem(int adapterPosition) {
        return mItems.get(adapterPosition);
    }

    public interface ProductItemListener {
        void onProductClick(long id);
        void onProductLongClick(Product product);
    }
}
