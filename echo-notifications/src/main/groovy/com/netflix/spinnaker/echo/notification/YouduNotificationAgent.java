package com.netflix.spinnaker.echo.notification;

import com.alibaba.fastjson.JSONObject;
import com.netflix.spinnaker.echo.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class YouduNotificationAgent extends AbstractEventNotificationAgent{
  @Autowired
  RestTemplate restTemplate;


  @Override
  public String getNotificationType() {
    return "youdu";
  }

  @Override
  public void sendNotifications(Map notification, String application, Event event, Map config, String status) {
    StringBuffer subject=new StringBuffer();
    StringBuffer message=new StringBuffer("To see more details, please visit:");
    Map execution = (Map) event.content.get("execution");
    if("pipeline".equals(config.get("type"))) {

      subject.append(application).append("'s").append(execution.get("name")).append(" pipeline");
      message.append(System.getProperty("line.separator"));
      message.append(getSpinnakerUrl()).append("/#/applications/")
        .append(application).append("/").append("executions").append("/").append(event.getContent().get("executionId"));
    } else if("stage".equals(config.get("type"))){
      String stageName=(String)event.content.get("name");
      subject.append("Stage ").append(stageName).append(" for ").append(application)
        .append("'s ").append(execution.get("name")).append(" pipeline");

      message.append(System.getProperty("line.separator"));
      message.append(getSpinnakerUrl()).append("/#/applications/")
        .append(application).append("/").append("executions").append("/").append(event.getContent().get("executionId"));

    }
    if("starting".equals(status)){
      subject.append(" is starting");
    }
    if("complete".equals(status)){
      subject.append(" has completed");
    }
    if("failed".equals(status)){
      subject.append(" is failed");
    }


    send(notification.get("userName").toString(),subject.append(System.getProperty("line.separator")).append(message).toString());


  }

  public boolean send(String users, String message) {
    String youduSendUrl="http://10.100.129.133:9000/youdu/sendMessage";
    try {
      String url = youduSendUrl;
      JSONObject requestJson=new JSONObject();

      requestJson.put("toUsers", users);
      requestJson.put("message", message);
      JSONObject resultJson = restTemplate.postForObject(url, requestJson, JSONObject.class);
      String result = resultJson.getString("result");
      if ("Ok".equals(result)) {
        return true;
      }
    } catch (Exception e) {

      e.printStackTrace();
      log.error("调用有度API失败: {}", e);
      return false;
    }
    return false;
  }

}
