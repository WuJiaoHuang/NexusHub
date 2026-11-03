package com.nexushub.listener;

import com.nexushub.search.PlaceChangedEvent;
import com.nexushub.search.PlaceSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;

@Slf4j
@Component
public class PlaceSearchSyncListener {

    @Resource
    private PlaceSearchService placeSearchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPlaceChanged(PlaceChangedEvent event) {
        try {
            switch (event.getType()) {
                case CREATED:
                case UPDATED:
                    placeSearchService.saveOrUpdate(event.getPlaceId());
                    break;
                case DELETED:
                    placeSearchService.delete(event.getPlaceId());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("地点搜索索引增量同步失败，placeId={}, type={}", event.getPlaceId(), event.getType(), e);
        }
    }
}
