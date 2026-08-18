/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.eclipse.paho.client.mqttv3.MqttConnectOptions
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.integration.channel.DirectChannel
 *  org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
 *  org.springframework.integration.mqtt.core.MqttPahoClientFactory
 *  org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
 *  org.springframework.messaging.MessageChannel
 */
package com.vrd.access.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {
    @Value(value="${mqtt.url}")
    private String url;
    @Value(value="${mqtt.username}")
    private String username;
    @Value(value="${mqtt.password}")
    private String password;
    @Value(value="${mqtt.client-id}")
    private String clientId;
    @Value(value="${mqtt.topic}")
    private String topic;
    @Value(value="${mqtt.qos}")
    private int qos;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{this.url});
        options.setUserName(this.username);
        options.setPassword(this.password.toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(this.clientId, this.mqttClientFactory(), new String[]{this.topic});
        adapter.setOutputChannel(this.mqttInputChannel());
        adapter.setQos(new int[]{this.qos});
        return adapter;
    }
}

