package com.cityeats.service;

import com.cityeats.common.BusinessException;
import com.cityeats.entity.VoucherOrder;
import com.cityeats.mapper.SeckillVoucherMapper;
import com.cityeats.mapper.VoucherOrderMapper;
import com.cityeats.service.impl.VoucherOrderTransactionalServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VoucherOrderTransactionalServiceTest {

    @Test
    void createVoucherOrderShouldUseConditionalStockUpdateBeforeInsert() {
        VoucherOrderMapper orderMapper = Mockito.mock(VoucherOrderMapper.class);
        SeckillVoucherMapper seckillMapper = Mockito.mock(SeckillVoucherMapper.class);
        when(orderMapper.selectCount(any())).thenReturn(0);
        when(seckillMapper.update(isNull(), any())).thenReturn(1);

        VoucherOrderTransactionalServiceImpl service = new VoucherOrderTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "voucherOrderMapper", orderMapper);
        ReflectionTestUtils.setField(service, "seckillVoucherMapper", seckillMapper);

        VoucherOrder order = new VoucherOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setVoucherId(1L);
        service.createVoucherOrder(order);

        verify(seckillMapper).update(isNull(), any());
        verify(orderMapper).insert(order);
    }

    @Test
    void createVoucherOrderShouldThrowWhenConditionalStockUpdateFails() {
        VoucherOrderMapper orderMapper = Mockito.mock(VoucherOrderMapper.class);
        SeckillVoucherMapper seckillMapper = Mockito.mock(SeckillVoucherMapper.class);
        when(orderMapper.selectCount(any())).thenReturn(0);
        when(seckillMapper.update(isNull(), any())).thenReturn(0);

        VoucherOrderTransactionalServiceImpl service = new VoucherOrderTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "voucherOrderMapper", orderMapper);
        ReflectionTestUtils.setField(service, "seckillVoucherMapper", seckillMapper);

        VoucherOrder order = new VoucherOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setVoucherId(1L);

        assertThrows(BusinessException.class, () -> service.createVoucherOrder(order));
        verify(seckillMapper).update(isNull(), any());
    }
}
