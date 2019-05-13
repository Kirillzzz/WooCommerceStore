package mx.niluxer.store;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import mx.niluxer.store.adapters.ProductsAdapter;
import mx.niluxer.store.data.model.Category;
import mx.niluxer.store.data.model.Image;
import mx.niluxer.store.data.model.Product;
import mx.niluxer.store.data.remote.ApiUtils;
import mx.niluxer.store.data.remote.WooCommerceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private WooCommerceService mService;
    private ProductsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = ApiUtils.getWooCommerceService();
        rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
        mAdapter = new ProductsAdapter(this, new ArrayList<Product>(0), new ProductsAdapter.ProductItemListener() {

            @Override
            public void onProductClick(long id) {
                Toast.makeText(MainActivity.this, "Product id is " + id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProductLongClick(Product product) {
                showContextualMenu(product);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(mAdapter);
        rvProducts.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rvProducts.addItemDecoration(itemDecoration);

        loadProducts();

    }

    public void loadProducts() {
        mService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {

                if(response.isSuccessful()) {
                    mAdapter.updateProducts(response.body());
                    Log.d("MainActivity", "products loaded from API");
                }else {
                    int statusCode  = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                //showErrorMessage();
                Log.d("MainActivity", "error loading from API");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.mnuAddProduct:
                NewEditProductDialog newEditProductDialog = new NewEditProductDialog(this);
                newEditProductDialog.show();
                break;
            case R.id.mnuExit:
                confirmExit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showContextualMenu(final Product product)
    {
        final CharSequence[] items = { "Edit", "Delete" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Action:");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                switch (item)
                {
                    case 0:
                        //Toast.makeText(MainActivity.this, product.getName(), Toast.LENGTH_SHORT).show();
                        NewEditProductDialog newEditProductDialog = new NewEditProductDialog(MainActivity.this);
                        newEditProductDialog.setEditMode(true);
                        newEditProductDialog.setProduct(product);
                        newEditProductDialog.show();

                        break;
                    case 1:
                        //Toast.makeText(MainActivity.this, product.getId() + "", Toast.LENGTH_SHORT).show();
                        mService.deleteProduct(product.getId()).enqueue(new Callback<Product>() {
                            @Override
                            public void onResponse(Call<Product> call, Response<Product> response) {
                                if (response.isSuccessful())
                                {
                                    Toast.makeText(MainActivity.this, "Product deleted successfully...", Toast.LENGTH_SHORT).show();
                                    loadProducts();
                                } else {
                                    try {
                                        System.out.println(response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Product> call, Throwable t) {
                                System.out.println("Error deleting product...");
                            }
                        });
                        break;
                }

            }

        });

        AlertDialog alert = builder.create();

        alert.show();
    }

    private void runThread()
    {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    InetAddress address = null;
                    try {
                        address = InetAddress.getByName("https://store");
                        System.out.println(address.getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void confirmExit()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Exit");
        builder.setMessage("Please confirm exit.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public class NewEditProductDialog extends android.app.AlertDialog {

        private boolean editMode = false;
        Context context;
        private Product product;

        protected NewEditProductDialog(Context context) {
            super(context);
            this.context = context;
        }

        public void setEditMode(boolean editMode)
        {
            this.editMode = editMode;
        }

        public void setProduct(Product product)
        {
            this.product = product;
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {


            String title = "New Product";
            if(editMode) title = "Edit Product";
            setTitle(title);
            setMessage("Product Details");
            View view = LayoutInflater.from(context).inflate(R.layout.new_edit_product, null);
            setView(view);

            final EditText txtProductName = view.findViewById(R.id.txtProductName);
            final EditText txtProductType = view.findViewById(R.id.txtProductType);
            final EditText txtProductCategory = view.findViewById(R.id.txtProductCategory);
            final EditText txtProductRegularPrice = view.findViewById(R.id.txtProductRegularPrice);
            final EditText txtProductDescription = view.findViewById(R.id.txtProductDescription);

            String btnText = "Send";
            if(editMode)
            {
                btnText = "Save";
                txtProductName.setText(product.getName());
                txtProductType.setText(product.getType());
                Category category = product.getCategories().get(0);
                txtProductCategory.setText(category.getId()+"");
                txtProductRegularPrice.setText(product.getRegularPrice());
                txtProductDescription.setText(product.getDescription());
            }


            setButton(DialogInterface.BUTTON_POSITIVE, btnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Product p = new Product();
                    Category category = new Category();
                    List<Category> categories = new ArrayList<Category>();
                    categories.add(category);
                    category.setId(Integer.valueOf(txtProductCategory.getText().toString()));
                    p.setName(txtProductName.getText().toString());
                    p.setType(txtProductType.getText().toString());
                    p.setCategories(categories);
                    p.setRegularPrice(txtProductRegularPrice.getText().toString());
                    p.setDescription(txtProductDescription.getText().toString());
                    Image image = new Image();
                    image.setSrc("http://172.20.11.60/~niluxer/wordpress/wp-content/uploads/2018/11/album-1-600x600.jpg");
                    image.setPosition(0);
                    List<Image> images = new ArrayList<>();
                    images.add(image);
                    p.setImages(images);
                    System.out.println("Sending...:" + p.toString());

                    if (editMode)
                    {
                        mService.saveEditedProduct(product.getId(), p).enqueue(new Callback<Product>() {
                            @Override
                            public void onResponse(Call<Product> call, Response<Product> response) {
                                if(response.isSuccessful())
                                {
                                    System.out.println(response.body().toString());
                                    Toast.makeText(context, "Product saved successfully", Toast.LENGTH_SHORT).show();
                                    loadProducts();
                                } else{

                                    try {
                                        System.out.println(response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Product> call, Throwable t) {
                                System.out.println("Error saving product...");
                            }
                        });

                    } else {
                        mService.saveProduct(p).enqueue(new Callback<Product>() {
                            @Override
                            public void onResponse(Call<Product> call, Response<Product> response) {
                                if(response.isSuccessful())
                                {
                                    System.out.println(response.body().toString());
                                    Toast.makeText(context, "Product saved successfully", Toast.LENGTH_SHORT).show();
                                    loadProducts();
                                } else{

                                    try {
                                        System.out.println(response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Product> call, Throwable t) {
                                System.out.println("Error saving product...");
                            }
                        });

                    }

                }
            });

            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

            super.onCreate(savedInstanceState);

        }
    }

}
