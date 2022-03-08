package com.himalaya.jpashop.service;

import com.himalaya.jpashop.domain.Address;
import com.himalaya.jpashop.domain.Member;
import com.himalaya.jpashop.domain.Order;
import com.himalaya.jpashop.domain.OrderStatus;
import com.himalaya.jpashop.domain.item.Book;
import com.himalaya.jpashop.domain.item.Item;
import com.himalaya.jpashop.exception.NotEnoughStockException;
import com.himalaya.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;
    
    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus()).as("상품 주문 시 상태는 ORDER")
                .isEqualTo(OrderStatus.ORDER);
        assertThat(getOrder.getOrderItems().size()).as("주문한 상품 종류 수가 정확해야 한다.")
                .isEqualTo(1);
        assertThat(getOrder.getTotalPrice()).as("주문 가격은 가격 * 수량이다.")
                .isEqualTo(10000 * orderCount);
        assertThat(book.getStockQuantity()).as("주문 수량만큼 재고가 줄어야 한다.")
                .isEqualTo(8);
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);
        
        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus()).as("주문 취소 시 상태는 CANCEL 이다.")
                .isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStockQuantity()).as("주문이 취소된 상품은 그만큰 재고가 증가해야 한다.")
                .isEqualTo(10);
    }
    
    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;
        //when
        orderService.order(member.getId(), item.getId(), orderCount);
        
        //then
        fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}