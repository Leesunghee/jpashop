package com.himalaya.jpashop.service;

import com.himalaya.jpashop.domain.Delivery;
import com.himalaya.jpashop.domain.Member;
import com.himalaya.jpashop.domain.Order;
import com.himalaya.jpashop.domain.OrderItem;
import com.himalaya.jpashop.domain.item.Item;
import com.himalaya.jpashop.repository.ItemRepository;
import com.himalaya.jpashop.repository.MemberRepository;
import com.himalaya.jpashop.repository.OrderRepository;
import com.himalaya.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     * @param memberId
     * @param itemId
     * @param count
     * @return
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        //엔티티 조회
        Member findMember = memberRepository.findOne(memberId);
        Item findItem = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(findMember.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(findItem, findItem.getPrice(), count);
        
        //주문생성
        Order order = Order.createOrder(findMember, delivery, orderItem);

        //주문저장
        orderRepository.save(order);

         return order.getId();
    }

    /**
     * 주문 취소
     * @param orderId
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order findOrder = orderRepository.findOne(orderId);
        //주문 취소
        findOrder.cancel();
    }

    //검색
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
