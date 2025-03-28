# ShopView

## Description

A simple online store with simulated orders. Available pages:
- Products list
- Product details
- Product creation form
- Shopping cart
- Order list
- Order details

The application allows users to:
- Add new products
- Add or remove products from the cart and adjust their quantities
- Place an order (checkout)


## Get started

- Build app docker image
```shell
make build_image
```

- Run app with infra in docker
```shell
make start
```

- Stop app with infra in docker
```shell
make stop
```

## Get started (dev mode)

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

- Delete previous images (if necessary)
```shell
make images_clean
```

- Copy images for test data (if necessary)
```shell
make images_copy
```

Main page with product catalog will be available at `http://127.0.0.1:8080/products`
