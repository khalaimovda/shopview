# ShopView

## Get started

- Up infra (database)
```shell
make infra_up
```

- Start app (and create database schema)
```shell
make app_start
```

- Clean data (if necessary)
```shell
make data_clean
# Password: password
```

- Fill test data (if necessary)
```shell
make data_init
# Password: password
```

- Main page with product catalog will be available at `http://127.0.0.1:8080/products`
