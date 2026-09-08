package com.campuslink.listener;

import com.campuslink.search.CampusPlaceChangedEvent;
import com.campuslink.search.CampusPlaceSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;

@Slf4j
@Component
public class CampusPlaceSearchSyncListener {

    @Resource
    private CampusPlaceSearchService placeSearchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCampusPlaceChanged(CampusPlaceChangedEvent event) {
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
            log.error("校园地点搜索索引增量同步失败，placeId={}, type={}", event.getPlaceId(), event.getType(), e);
        }
    }
}
