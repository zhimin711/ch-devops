package com.ch.cloud.rocketmq.manager.impl;

import com.ch.cloud.rocketmq.manager.RMQMessageManager;
import com.ch.cloud.rocketmq.model.MessageView;
import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.rocketmq.common.message.MessageDecoder.MSG_ID_LENGTH;

/**
 * <p>
 * desc: RMQMessageManagerImpl
 * </p>
 *
 * @author zhimin
 * @since 2025/6/11 09:43
 */
@Service
@Slf4j
public class RMQMessageManagerImpl implements RMQMessageManager {
    
    /**
     * @see org.apache.rocketmq.store.config.MessageStoreConfig maxMsgsNumBatch = 64;
     * @see org.apache.rocketmq.store.index.IndexService maxNum = Math.min(maxNum,
     * this.defaultMessageStore.getMessageStoreConfig().getMaxMsgsNumBatch());
     */
    private final static int QUERY_MESSAGE_MAX_NUM = 64;
    
    public Pair<MessageView, List<MessageTrack>> viewMessage(String subject, final String msgId) throws Exception {
        MessageExt messageExt = RMQAdminUtil.getClient().viewMessage(subject, msgId);
        if (messageExt == null) {
            return null;
        }
        List<MessageTrack> messageTrackList = messageTrackDetail(messageExt);
        return new Pair<>(MessageView.fromMessageExt(messageExt), messageTrackList);
    }
    
    @Override
    public List<MessageView> queryMessageByTopicAndKey(String topic, String key) {
        try {
            return RMQAdminUtil.getClient()
                    .queryMessage(topic, key, QUERY_MESSAGE_MAX_NUM, 0, System.currentTimeMillis()).getMessageList()
                    .stream().map(MessageView::fromMessageExt).collect(Collectors.toList());
        } catch (Exception err) {
            throw Throwables.propagate(err);
        }
    }
    
    @Override
    public List<MessageView> queryMessageByTopic(String topic, final long begin, final long end) {
        DefaultMQPullConsumer consumer = RMQAdminUtil.createConsumer(MixAll.TOOLS_CONSUMER_GROUP, null);
        List<MessageView> messageViewList = Lists.newArrayList();
        try {
            String subExpression = "*";
            consumer.start();
            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
            for (MessageQueue mq : mqs) {
                long minOffset = consumer.searchOffset(mq, begin);
                //                int  beginOffset = consumer.getOffsetInQueueByTime(topic, i, timeStamp);
                long maxOffset = consumer.searchOffset(mq, end);
                READQ:
                for (long offset = minOffset; offset <= maxOffset; ) {
                    try {
                        if (messageViewList.size() > 2000) {
                            break;
                        }
                        PullResult pullResult = consumer.pull(mq, subExpression, offset, 32);
                        offset = pullResult.getNextBeginOffset();
                        switch (pullResult.getPullStatus()) {
                            case FOUND:
                                List<MessageView> messageViewListByQuery = pullResult.getMsgFoundList().stream()
                                        .map(messageExt -> {
                                            messageExt.setBody(null);
                                            return MessageView.fromMessageExt(messageExt);
                                        }).collect(Collectors.toList());
                                List<MessageView> filteredList = Lists.newArrayList(
                                        messageViewListByQuery.stream().filter(messageView -> {
                                            if (messageView.getStoreTimestamp() < begin
                                                    || messageView.getStoreTimestamp() > end) {
                                                log.info("begin={} end={} time not in range {} {}", begin, end,
                                                        messageView.getStoreTimestamp(),
                                                        new Date(messageView.getStoreTimestamp()).toString());
                                            }
                                            return messageView.getStoreTimestamp() >= begin
                                                    && messageView.getStoreTimestamp() <= end;
                                        }).collect(Collectors.toList()));
                                messageViewList.addAll(filteredList);
                                break;
                            case NO_MATCHED_MSG:
                            case NO_NEW_MSG:
                            case OFFSET_ILLEGAL:
                                break READQ;
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            messageViewList.sort((o1, o2) -> {
                if (o1.getStoreTimestamp() - o2.getStoreTimestamp() == 0) {
                    return 0;
                }
                return (o1.getStoreTimestamp() > o2.getStoreTimestamp()) ? 1 : -1;
            });
            return messageViewList;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            consumer.shutdown();
        }
    }
    
    @Override
    public List<MessageTrack> messageTrackDetail(MessageExt msg) {
        try {
            return RMQAdminUtil.getClient().messageTrackDetail(msg);
        } catch (Exception e) {
            log.error("op=messageTrackDetailError", e);
            return Collections.emptyList();
        }
    }
    
    
    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(String topic, String msgId, String consumerGroup,
            String clientId) {
        if (StringUtils.isNotBlank(clientId)) {
            try {
                return RMQAdminUtil.getClient().consumeMessageDirectly(consumerGroup, clientId, topic, msgId);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        
        try {
            ConsumerConnection consumerConnection = RMQAdminUtil.getClient().examineConsumerConnectionInfo(consumerGroup);
            for (Connection connection : consumerConnection.getConnectionSet()) {
                if (StringUtils.isBlank(connection.getClientId())) {
                    continue;
                }
                log.info("clientId={}", connection.getClientId());
                return RMQAdminUtil.getClient().consumeMessageDirectly(consumerGroup, connection.getClientId(), topic, msgId);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        throw new IllegalStateException("NO CONSUMER");
        
    }
    
    @Override
    public Pair<MessageView, List<MessageTrack>> viewMessageByBrokerAndOffset(String brokerHost, int port, long offset)
            throws Exception {
        ByteBuffer byteBufferMsgId = ByteBuffer.allocate(MSG_ID_LENGTH);
        SocketAddress brokerHostAddress = new InetSocketAddress(brokerHost, port);
        String msgId = MessageDecoder.createMessageId(byteBufferMsgId,
                MessageExt.socketAddress2ByteBuffer(brokerHostAddress), offset);
        return viewMessage(null, msgId);
    }
    
}
