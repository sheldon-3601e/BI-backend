package com.sheldon.springbootinit.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName AiManager
 * @Author 26483
 * @Date 2024/1/22 17:11
 * @Version 1.0
 * @Description TODO
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient client;

    public String doChart(Long modelId, String data) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(data);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        // System.out.println(response.getData());
        return response.getData().getContent();
    }
}
