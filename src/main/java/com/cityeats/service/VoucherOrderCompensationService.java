package com.cityeats.service;

import com.cityeats.dto.VoucherOrderMessage;

public interface VoucherOrderCompensationService {
    Long compensate(VoucherOrderMessage message);
}
