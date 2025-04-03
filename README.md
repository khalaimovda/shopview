# ShopView

## Description

A simple online store with simulated orders. Reactive stack.

Available pages:
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

- Build app docker image and start it
```shell
docker compose up -d --build
```

- Fill test data
```shell
docker-compose run --rm db-init
```

Main page with product catalog will be available at `http://127.0.0.1:8080/products`
