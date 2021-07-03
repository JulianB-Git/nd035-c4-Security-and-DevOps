package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.LoginRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

    @InjectMocks
    private ControllerTest userController;

    @InjectMocks
    private CartController cartController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PasswordEncoder encoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockHttpServletRequest request;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserRequest userRequest;

    @Test
    public void testCreateUser() throws Exception {
        userRequest = new CreateUserRequest();

        userRequest.setUsername("Julian");
        userRequest.setPassword("Password1");
        userRequest.setConfirmPassword("Password1");

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").exists()).andReturn();
    }

    @Test
    public void testCreateUserUsernameExists() throws Exception {
        userRequest = new CreateUserRequest();

        userRequest.setUsername("Julian1");
        userRequest.setPassword("Password1");
        userRequest.setConfirmPassword("Password1");

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").exists()).andReturn();

        //Second attempt at same username
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("This username already exists")).andReturn();
    }

    @Test
    public void testCreateUserPasswordLengthFail() throws Exception {
        userRequest = new CreateUserRequest();

        userRequest.setUsername("Julian1");
        userRequest.setPassword("Passwo");
        userRequest.setConfirmPassword("Passwo");

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password length must be more that 8 characters")).andReturn();

    }

    @Test
    public void testCreateUserPasswordDoNotMatch() throws Exception {
        userRequest = new CreateUserRequest();

        userRequest.setUsername("Julian2");
        userRequest.setPassword("Password1");
        userRequest.setConfirmPassword("Password2");

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("The entered passwords do not match")).andReturn();

    }

    @Test
    public void testFindUserByUsernameAndId() throws Exception {
        userRequest = new CreateUserRequest();

        userRequest.setUsername("Julian3");
        userRequest.setPassword("Password1");
        userRequest.setConfirmPassword("Password1");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").exists()).andReturn();

        //Map response data to User object
        String content = mvcResult.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);

        LoginRequest loginRequest = new LoginRequest();
        BeanUtils.copyProperties(userRequest, loginRequest);

        //Login newly created user
        MvcResult response = mockMvc.perform(
                MockMvcRequestBuilders.post("/login").content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Retrieve Bearer token
        String token = response.getResponse().getHeaderValue("Authorization").toString();

        //Get user by username
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user/"+userRequest.getUsername()).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value(loginRequest.getUsername())).andReturn();

        //Get user by id
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user/id/"+user.getId()).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").value(user.getUsername())).andReturn();

    }

//    @Test
//    public void testFindUserById() throws Exception {
//
//    }

    @Test
    public void testAddToCartInvalidUser() throws Exception{
        ModifyCartRequest cartRequest = new ModifyCartRequest();

        cartRequest.setUsername("Julian");
        cartRequest.setItemId(1L);
        cartRequest.setQuantity(1);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart/addToCart").header("Authorization", getBearerToken("invalid")).content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found")).andReturn();
    }

    @Test
    public void testAddToCartInvalidItem() throws Exception{
        ModifyCartRequest cartRequest = new ModifyCartRequest();

        cartRequest.setUsername("Julian100");
        cartRequest.setItemId(7L);
        cartRequest.setQuantity(1);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart/addToCart").header("Authorization", getBearerToken("Julian100")).content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Item not found")).andReturn();
    }

    @Test
    public void testAddToCartSuccess() throws Exception{
        ModifyCartRequest cartRequest = new ModifyCartRequest();

        cartRequest.setUsername("Julian101");
        cartRequest.setItemId(1L);
        cartRequest.setQuantity(1);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart/addToCart").header("Authorization", getBearerToken("Julian101")).content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L)).andReturn();
    }

    @Test
    public void testRemoveFromCartSuccess() throws Exception{
        ModifyCartRequest cartRequest = new ModifyCartRequest();

        cartRequest.setUsername("Julian102");
        cartRequest.setItemId(1L);
        cartRequest.setQuantity(1);

        String token = getBearerToken("Julian102");

        //Add to cart
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart/addToCart").header("Authorization", token).content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.total").value("2.99")).andReturn();

        //Remove from cart
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart/removeFromCart").header("Authorization", token).content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.total").value("0.0")).andReturn();
    }

    @Test
    public void testSubmitOrderEmptyCart() throws Exception{
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/order/submit/Julian200").header("Authorization", getBearerToken("Julian200"))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Cannot submit order because the users cart is empty")).andReturn();
    }

    @Test
    public void testSubmitOrderInvalidUser() throws Exception{
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/order/submit/Julia").header("Authorization", getBearerToken("Julian205"))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found")).andReturn();
    }

    @Test
    public void testSubmitOrderSuccess() throws Exception{
        ModifyCartRequest cartRequest = new ModifyCartRequest();

        cartRequest.setUsername("Julian201");
        cartRequest.setItemId(2L);
        cartRequest.setQuantity(2);

        String token = getBearerToken("Julian201");

        //Add to cart
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart/addToCart").header("Authorization", token).content(objectMapper.writeValueAsString(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.total").value("3.98")).andReturn();

        //Submit order
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/order/submit/Julian201").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.total").value("3.98")).andReturn();
    }

    private String getBearerToken(String username) throws Exception {
        CreateUserRequest userRequest = new CreateUserRequest();

        userRequest.setUsername(username);
        userRequest.setPassword("Password1");
        userRequest.setConfirmPassword("Password1");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.username").exists()).andReturn();

        //Map response data to User object
        String content = mvcResult.getResponse().getContentAsString();
        User user = objectMapper.readValue(content, User.class);

        LoginRequest loginRequest = new LoginRequest();
        BeanUtils.copyProperties(userRequest, loginRequest);

        //Login newly created user
        MvcResult response = mockMvc.perform(
                MockMvcRequestBuilders.post("/login").content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Retrieve Bearer token
        return response.getResponse().getHeaderValue("Authorization").toString();
    }

}
