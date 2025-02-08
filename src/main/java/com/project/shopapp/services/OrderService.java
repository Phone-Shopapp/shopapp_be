package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.zip.DataFormatException;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMappper;


    @Override
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() ->
                        new DataNotFoundException("Người dùng không tồn tại"));

//        //convert DTO into order, skip id
//        modelMappper.typeMap(OrderDTO.class, Order.class)
//                .addMappings(mapper -> mapper.skip(Order::setId));

        Order order = new Order();
        modelMappper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        order.setStatus(OrderStatus.PENDING);
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")) : orderDTO.getShippingDate();

        if (shippingDate.isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new DataNotFoundException("Thời gian ship ít nhất là ngày hôm nay");
        }
        order.setActive(true);
        order.setShippingDate(shippingDate);
        orderRepository.save(order);
        return order;
    }

    @Override
    public Order getOrder(Long id) throws DataNotFoundException {
        return orderRepository
                .findById(id)
                .orElseThrow(() ->
                        new DataNotFoundException("Không tìm thấy sản phẩm"));
    }

    @Override
    public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order existingOrder = orderRepository
                .findById(id)
                .orElseThrow(() ->
                        new DataNotFoundException("Không tìm thấy sản phẩm"));

        User existingUser = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() ->
                        new DataNotFoundException("Không tìm thấy người dùng"));

        modelMappper.map(orderDTO, existingOrder);
        existingOrder.setUser(existingUser);
        return orderRepository.save(existingOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        Order optionalOrder =
                orderRepository.findById(id).orElse(null);

        if (optionalOrder != null) {
            optionalOrder.setActive(false);
            orderRepository.save(optionalOrder);
        }

    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
