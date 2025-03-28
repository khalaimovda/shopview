package com.github.khalaimovda.shopview.integration;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductDetail;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static com.github.khalaimovda.shopview.utils.ImageUtils.createRandomBytes;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
public class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("products"))
            // Check that the model contains a pagination object ("page")
            .andExpect(model().attributeExists("page"))
            // Check that the pagination container is present
            .andExpect(content().string(containsString("class=\"pagination-container\"")))
            // Check that at least one product block is present
            .andExpect(content().string(containsString("class=\"product\"")))

            // Check specific values for the first product (from the end) (s = 25)
            .andExpect(content().string(containsString("Товар 25")))
            .andExpect(content().string(containsString("25.99"))) // Price: 0.99 + 25 = 25.99
            .andExpect(content().string(containsString("src=\"/images/image_path_25.png\"")))

            // Check specific values for the second product (from the end) (s = 24)
            .andExpect(content().string(containsString("Товар 24")))
            .andExpect(content().string(containsString("24.99"))) // Price: 0.99 + 24 = 24.99
            .andExpect(content().string(containsString("src=\"/images/image_path_24.png\"")))

            // Check specific values for the last product on the page (25 - 20 + 1 = 6) (s = 6)
            .andExpect(content().string(containsString("Товар 6")))
            .andExpect(content().string(containsString("6.99"))) // Price: 0.99 + 6 = 6.99
            .andExpect(content().string(containsString("src=\"/images/image_path_6.png\"")));
    }

    @Test
    void testCreateProduct() throws Exception {
        String productName = "TestName";
        String productDescription = "TestDescription";
        String imageName = "test-create.jpg";
        BigDecimal productPrice = new BigDecimal("82.13");

        MockMultipartFile imageFile = new MockMultipartFile("image", imageName, "image/jpeg", createRandomBytes());
        ProductCreateForm form = ProductCreateForm.builder()
            .name(productName)
            .description(productDescription)
            .image(imageFile)
            .price(productPrice)
            .build();

        mockMvc.perform(multipart("/products")
                .file(imageFile)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .formField("name", form.getName())
                .formField("description", form.getDescription())
                .formField("price", String.valueOf(form.getPrice())))
            .andExpect(status().isCreated());

        Optional<Product> optionalProduct = productRepository.findByName(productName);
        assertTrue(optionalProduct.isPresent());
        Product product = optionalProduct.get();

        assertAll(
            () -> assertEquals(productName, product.getName()),
            () -> assertEquals(productDescription, product.getDescription()),
            () -> assertTrue(product.getImagePath().endsWith(imageName)),
            () -> assertEquals(productPrice, product.getPrice())
        );
    }

    @Test
    void testGetProductById() throws Exception {
        // Based on predefined sql
        ProductDetail expectedProductDetail = new ProductDetail();
        expectedProductDetail.setId(1L);
        expectedProductDetail.setName("Товар 1");
        expectedProductDetail.setDescription("Описание 1");
        expectedProductDetail.setImagePath("/images/image_path_1.png");
        expectedProductDetail.setPrice(new BigDecimal("1.99"));
        expectedProductDetail.setCount(0); // First product is not in cart

        mockMvc.perform(get("/products/" + expectedProductDetail.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("product"))

            .andExpect(content().string(containsString(expectedProductDetail.getName())))
            .andExpect(content().string(containsString(expectedProductDetail.getDescription())))
            .andExpect(content().string(containsString(expectedProductDetail.getImagePath())))
            .andExpect(content().string(containsString(expectedProductDetail.getPrice().toString())))
            .andExpect(content().string(containsString(String.valueOf(expectedProductDetail.getCount()))));
    }
}
