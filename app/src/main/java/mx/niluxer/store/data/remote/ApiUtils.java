package mx.niluxer.store.data.remote;

import mx.niluxer.store.Config;

public class ApiUtils {
    public static final String BASE_URL = Config.BASE_URL;

    public static WooCommerceService getWooCommerceService() {
        return RetrofitClient.getClient(BASE_URL).create(WooCommerceService.class);
    }
}
