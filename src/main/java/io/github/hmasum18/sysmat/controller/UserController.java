package io.github.hmasum18.sysmat.controller;

import io.github.hmasum18.sysmat.model.Category;
import io.github.hmasum18.sysmat.model.Product;
import io.github.hmasum18.sysmat.service.CategoryService;
import io.github.hmasum18.sysmat.service.ProductService;
import io.github.hmasum18.sysmat.service.UserProfileService;
import io.github.hmasum18.sysmat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@Controller
@RequestMapping("user")
public class UserController {

    @Value("${firebase-api-key}")
    private String firebaseApiKey;

    @Value("${firebase-project-id}")
    private String firebaseProjectId;

    @Value("${firebase-message-sender-id}")
    private String messagingSenderId;

    @Value("${firebase-app-id}")
    private String firebaseAppId;

    @Value("${geoapify-api-key}")
    String geoapifyApiKey;

    @Autowired
    CategoryService categoryService;

    @Autowired
    UserService userService;

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    ProductService productService;

    // =============================== product routes ======================================
    @GetMapping("product/add")
    public String addProduct(ModelMap modelMap) {
        modelMap.put("product", new Product());
        modelMap.put("userProfile", userProfileService.getProfile(userService.getLoggedInUser()));
        modelMap.put("categoryList", categoryService.getAllCategory());
        modelMap.put("geoapifyApiKey", geoapifyApiKey);
        putFirebaseCredentials(modelMap);
        return "product/add_product";
    }


    @PostMapping("product/add")
    public String addProduct(@ModelAttribute("product") Product product, int categoryId) {
        Optional<Category> categoryOptional = categoryService.getCategory(categoryId);
        product.setCategory(categoryOptional.get());
        product.setUser(userService.getLoggedInUser());
        product = productService.save(product);
        System.out.println("addProduct(): " + product);

        return "redirect:/user/product/";
    }

    @GetMapping(value = "product/{productId}/edit/")
    public String editProduct(@PathVariable int productId, ModelMap modelMap ) {
        Optional<Product> optionalProduct = productService.getProduct(productId);
        optionalProduct.ifPresent(product -> {
            modelMap.put("product", product);
        });

        modelMap.put("categoryList", categoryService.getAllCategory());
        modelMap.put("geoapifyApiKey", geoapifyApiKey);
        putFirebaseCredentials(modelMap);
        return "product/edit_product";
    }

    @PostMapping("/product/{productId}/edit")
    public String editProduct(@ModelAttribute("product") Product product, int categoryId, @PathVariable int productId) {
        Optional<Category> categoryOptional = categoryService.getCategory(categoryId);
        product.setCategory(categoryOptional.get());

        if(product.getUser().getId() == userService.getLoggedInUser().getId()){

        }

        Optional<Product> prevProduct = productService.getProduct(productId);
        product.setCreated(prevProduct.get().getCreated());
        product.setVerified(prevProduct.get().isVerified());
        product.setUser(prevProduct.get().getUser());

        product = productService.save(product);
        System.out.println("addProduct(): " + product);

        if(userService.getLoggedInUser().getRoles().equals("ROLE_ADMIN")){
            return "redirect:/admin/product/pending/";
        }

        return "redirect:/user/product/";
    }

    //working file
    @PostMapping("/product/{productId}/delete")
    public String deleteProduct(@PathVariable int productId) {
        System.out.println("deleteProduct(): " + productId);
        productService.delete(productId);
        return "redirect:/user/product/";
    }

    @GetMapping("product/")
    public String getAllProducts(ModelMap modelMap) {
        modelMap.put("productListUnverified",
                productService.getAllProduct(userService.getLoggedInUser(), false).get());
        modelMap.put("productList",
                productService.getAllProduct(userService.getLoggedInUser(), true).get());

        putFirebaseCredentials(modelMap);
        return "product/all_products";
    }

    private void putFirebaseCredentials(ModelMap modelMap) {
        modelMap.put("firebaseApiKey", firebaseApiKey);
        modelMap.put("firebaseProjectId", firebaseProjectId);
        modelMap.put("messagingSenderId", messagingSenderId);
        modelMap.put("firebaseAppId", firebaseAppId);
    }
}
