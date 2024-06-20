package org.zerock.b01.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import org.zerock.b01.constant.ItemSellStatus;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemListAllDTO {

    private Long ino;

    private String i_name;

    private int i_price; // 성은추가

    private String i_color; //i_price 에서 변경

    private String i_size; // 성은추가/

    private int i_stock; //재고보유여부
    private ItemSellStatus itemSellStatus; // 추가된 부분

    private List<ItemImageDTO> itemImages;


    // 추가된 생성자
    public ItemListAllDTO(Long ino, String i_name, int i_price, String i_color, String i_size, int i_stock, ItemSellStatus itemSellStatus) {
        this.ino = ino;
        this.i_name = i_name;
        this.i_price = i_price;
        this.i_color = i_color;
        this.i_size = i_size;
        this.i_stock = i_stock;
        this.itemSellStatus = itemSellStatus;
    }





}
