package com.cityeats.listener;

import com.cityeats.search.ShopChangedEvent;
import com.cityeats.search.ShopSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;

@Slf4j
@Component
public class ShopSearchSyncListener {

    @Resource
    private ShopSearchService shopSearchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onShopChanged(ShopChangedEvent event) {
        try {
            switch (event.getType()) {
                case CREATED:
                case UPDATED:
                    shopSearchService.saveOrUpdate(event.getShopId());
                    break;
                case DELETED:
                    shopSearchService.delete(event.getShopId());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("商户搜索索引增量同步失败，shopId={}, type={}", event.getShopId(), event.getType(), e);
        }
    }
}
