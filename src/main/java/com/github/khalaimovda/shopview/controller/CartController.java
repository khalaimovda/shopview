package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.dto.Cart;
import com.github.khalaimovda.shopview.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("")
    public String getCart(Model model) {
        Optional<Cart> cart = cartService.getCart();
        model.addAttribute("cart", cart.orElse(null));
        return "cart";
        // todo: Что нам нужно в cart: orderId; List<Product> (без description, без imagePath, но с totalPrice); totalPrice
        // Так как мы должны иметь возможноть регулировать доваление товаров в корзине тоже
        // то, видимо, все totalPrice (в том числе и финальный) надо считать на фронтенде

        // Наверное, здесь, я не пойду по окончательному хорошему пути
        // Пусть при первом запросе он все считает и возвращает. А при последующих --нет
        // Возможно, можно добавить перед оформление заказа get-запрос на финальную цену заказа и, если оно совпадает, то формировать заказ
    }

    @PostMapping("/add/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void addProductToCart(@PathVariable("productId") @Min(1L) Long productId) {
        cartService.addProductToCart(productId);
    }

    @PostMapping("/decrease/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void decreaseProductInCart(@PathVariable("productId") @Min(1L) Long productId) {
        cartService.decreaseProductInCart(productId);
    }

    @DeleteMapping("/remove/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeProductFromCart(@PathVariable("productId") @Min(1L) Long productId) {
        cartService.removeProductFromCart(productId);
    }

}
