package org.zerock.b01.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CartDTO {

    private Long cartItemId; // 장바구니 상품 아이디
    private String itemNm; // 상품명
    private int price; // 상품 금액
    private String imgUrl; // 상품 이미지 경로

    public CartDTO(Long cartItemId, String itemNm, int price, String imgUrl) {
        this.cartItemId = cartItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.imgUrl = imgUrl;
    }

}
