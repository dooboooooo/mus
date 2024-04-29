package org.zerock.b01.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.b01.domain.Board;
import org.zerock.b01.domain.Orders;
import org.zerock.b01.dto.*;
import org.zerock.b01.repository.BoardRepository;
import org.zerock.b01.repository.OrdersRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class OrdersServiceImpl implements OrdersService{

    private final ModelMapper modelMapper;

    private final OrdersRepository ordersRepository;


    @Override
    public Long register(OrdersListDTO ordersListDTO) {
        Orders orders = dtoToEntity(ordersListDTO);
        return null;
    }

    @Override
    public OrdersListDTO readOne(Long o_no) {
        return null;
    }

    @Override
    public PageResponseDTO<OrdersListDTO> listWithAll(OrdersPageRequestDTO ordersPageRequestDTO) {
        return null;
    }
}