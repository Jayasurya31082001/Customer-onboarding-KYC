package com.example.accountservice.corebanking.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class RestCoreBankingClientTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient client;

    private RestCoreBankingClient restCoreBankingClient;

    @BeforeEach
    void setUp() {
        restCoreBankingClient = new RestCoreBankingClient(builder, "http://core-banking");
    }

    @Test
    void provisionAccount_postsRequestAndReturnsResponse() {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl("http://core-banking")).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any(Object.class));
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CoreBankingProvisionResponse.class)).thenReturn(new CoreBankingProvisionResponse("110011"));

        CoreBankingProvisionResponse response = restCoreBankingClient.provisionAccount("cust-1", null);

        assertThat(response.sortCode()).isEqualTo("110011");
    }

    @Test
    void fallbackProvision_wrapsAsCoreBankingUnavailableException() {
        assertThatThrownBy(() -> restCoreBankingClient.fallbackProvision("cust-1", "corr-1", new RuntimeException("boom")))
                .isInstanceOf(CoreBankingUnavailableException.class)
                .hasMessageContaining("cust-1");
    }
}
