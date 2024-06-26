package org.zerock.b01.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.zerock.b01.domain.Board;
import org.zerock.b01.domain.Item;
import org.zerock.b01.dto.*;
import org.zerock.b01.repository.ItemRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.zerock.b01.constant.ItemSellStatus;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService{
    private final ModelMapper modelMapper;

    private final ItemRepository itemRepository;

//    @Override
//    public Long register(ItemDTO itemDTO){
//        Item item = modelMapper.map(itemDTO, Item.class);
//        return itemRepository.save(item).getIno();
//    }
//    /*asd*/

    @Override
    public Long register(ItemDTO itemDTO) {
        log.info("===========================================" + itemDTO);
        Item item = dtoToEntity(itemDTO);
        log.info(item);
        Long ino = itemRepository.save(item).getIno();
        log.info(ino);
        return ino;
    }


//    @Override
//    public ItemDTO readOne(Long ino) {
//
//
//       Optional<Item> result = itemRepository.findById(ino);
//
//        Item item = result.orElseThrow();
//
//       ItemDTO itemDTO = modelMapper.map(item, ItemDTO.class);
//
//       return itemDTO;
//    }

    @Override
    public ItemDTO readOne(Long ino) {
        log.info(ino);

        //item_image까지 조인 처리되는 findByWithImages()를 이용
        Optional<Item> result = itemRepository.findByIdWithImages(ino);
        log.info("itemRepository.findByIdWithImages(ino) 결과값=" + result);

        Item item = result.orElseThrow();

        log.info("null값이 아닐때 result.orElseThrow() "+ item);

        ItemDTO itemDTO = entityToDTO(item);
        log.info("entityToDTO 변경후 "+ itemDTO);

        return itemDTO;
    }

//    @Override
//    public void modify(ItemDTO itemDTO) {
//
//        Optional<Item> result = itemRepository.findById(itemDTO.getIno());
//
//        Item item = result.orElseThrow();
//
//        item.change(itemDTO.getI_name(),itemDTO.getI_price(),itemDTO.getI_color(),itemDTO.getI_size());
//
//        itemRepository.save(item);
//    }

    @Override
    public void modify(ItemDTO itemDTO) {

        Optional<Item> result = itemRepository.findById(itemDTO.getIno());

        Item item = result.orElseThrow();


        //첨부파일의 처리
        item.clearImages();

        if(itemDTO.getFileNames() != null){
            for (String fileName : itemDTO.getFileNames()) {
                String[] arr = fileName.split("_");
                item.addImage(arr[0], arr[1]);
            }
        }

        itemRepository.save(item);

    }

    @Override
    public void remove(Long ino) {
        itemRepository.deleteById(ino);
    }

    @Override
    public void addStock(Long ino, int quantity) {
        Item item = itemRepository.findById(ino).orElseThrow(() -> new IllegalArgumentException("Invalid item id"));
        item.addStock(quantity);
        itemRepository.save(item);
    }

    @Override
    public void removeStock(Long ino, int quantity) {
        Item item = itemRepository.findById(ino).orElseThrow(() -> new IllegalArgumentException("Invalid item id"));
        item.removeStock(quantity);
        itemRepository.save(item);
    }

    @Override
    public ItemPageResponseDTO<ItemDTO> list(ItemPageRequestDTO itemPageRequestDTO) {
        String[] types = itemPageRequestDTO.getTypes();
        String keyword = itemPageRequestDTO.getKeyword();
        Pageable pageable = itemPageRequestDTO.getPageable("ino");

        Page<Item> result = itemRepository.searchAll(types, keyword, pageable);

        List<ItemDTO> dtoList = result.getContent().stream()
                .map(item -> modelMapper.map(item, ItemDTO.class)).collect(Collectors.toList());


        return ItemPageResponseDTO.<ItemDTO>withAll()
                .itemPageRequestDTO(itemPageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();

    }

    @Override
    public ItemPageResponseDTO<ItemListAllDTO> listWithAll(ItemPageRequestDTO itemPageRequestDTO) {

        String[] types = itemPageRequestDTO.getTypes();
        String keyword = itemPageRequestDTO.getKeyword();
        Pageable pageable = itemPageRequestDTO.getPageable("ino");

        Page<ItemListAllDTO> result = itemRepository.searchWithAll(types, keyword, pageable);

        return ItemPageResponseDTO.<ItemListAllDTO>withAll()
                .itemPageRequestDTO(itemPageRequestDTO)
                .dtoList(result.getContent())
                .total((int)result.getTotalElements())
                .build();
    }

    @Override // 하나만 읽어오고 ItemListAllDTO로 가져온다.
    public ItemListAllDTO selectItemListAllDTO(Long ino) {
        log.info("받은 ino : " + ino);
        Optional<Item> result = itemRepository.findByIdWithImages(ino);
        Item item = result.orElseThrow();
        ItemListAllDTO itemListAllDTO = ItemListAllDTO.builder()
                .ino(item.getIno())
                .i_name(item.getI_name())
                .i_price(item.getI_price())
                .i_color(item.getI_color())
                .i_size(item.getI_size())
                .i_stock(item.getI_stock())
                .itemSellStatus(item.getItemSellStatus()) // 추가된 부분
                .itemImages(item.getItemImageSet().stream().map(itemImage -> ItemImageDTO.builder()
                        .uuid(itemImage.getUuid())
                        .fileName(itemImage.getFileName())
                        .ord(itemImage.getOrd())
                        .build()).collect(Collectors.toList()))
                .build();
        return itemListAllDTO;
    }



}
