/**
 * Listener class to listen to kafka topic.
 */
package com.org.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RefreshScope
public class ListenerService {

	RestTemplate restTemplate = new RestTemplate();

	@Value("${crudendpoint}")
	String endpoint;

	@org.springframework.kafka.annotation.KafkaListener(topics = "${topic}", groupId = "test-group")
	@SendTo
	public String process(String message, @Headers MessageHeaders headers) {

		String svcResp = null;
		String operReqd = new String((byte[]) headers.get("operation"));

		log.info("Endpoint to invoke {}", endpoint);

		switch (operReqd) {

		case "read_from_cassandra":

			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(endpoint).queryParam("tableName",
					message);
			svcResp = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, null, String.class).getBody();
			break;

		case "insert_to_cassandra":
			svcResp = restTemplate.exchange(endpoint, HttpMethod.POST, getRequestEntity(message), String.class)
			.getBody();
			break;

		case "update_to_cassandra":
			svcResp = restTemplate.exchange(endpoint, HttpMethod.PUT, getRequestEntity(message), String.class)
			.getBody();
			break;

		case "delete_from_cassandra":
			svcResp = restTemplate.exchange(endpoint, HttpMethod.DELETE, getRequestEntity(message), String.class)
			.getBody();
			break;
		}

		return svcResp;
	}

	/**
	 * Create request entity for service call.
	 * 
	 * @param {@linkplain String}.
	 * @return {@linkplain HttpEntity<String>}.
	 */
	private HttpEntity<String> getRequestEntity(String message) {

		HttpHeaders reqHeaders = new HttpHeaders();
		reqHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<String>(message, reqHeaders);
	}

}