package edu.eci.patriciaM12;

import edu.eci.patriciaM12.domain.model.MetricEvent;
import edu.eci.patriciaM12.domain.model.enums.MetricEventType;
import edu.eci.patriciaM12.domain.model.enums.PatchCategory;
import edu.eci.patriciaM12.domain.ports.in.ProcessMetricEventUseCase;
import edu.eci.patriciaM12.infrastructure.adapters.messaging.KafkaMetricEventConsumerAdapter;
import edu.eci.patriciaM12.infrastructure.adapters.messaging.dto.InboundPatchEventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerAdapterTest {

    @Mock private ProcessMetricEventUseCase processMetricEventUseCase;

    private KafkaMetricEventConsumerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new KafkaMetricEventConsumerAdapter(processMetricEventUseCase);
    }

    @Test
    void convierteJoinMessageADomainEvent() {
        UUID userId = UUID.randomUUID();
        UUID patchId = UUID.randomUUID();
        InboundPatchEventMessage msg = buildMessage("JOIN", userId, patchId, PatchCategory.SPORTS);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        MetricEvent domain = captor.getValue();
        assertThat(domain.getEventType()).isEqualTo(MetricEventType.JOIN);
        assertThat(domain.getPayload().get("userId")).isEqualTo(userId.toString());
        assertThat(domain.getPayload().get("patchCategory")).isEqualTo("SPORTS");
        assertThat(domain.getSourceModule()).isEqualTo("M06");
    }

    @Test
    void mapearViewCorrectamente() {
        InboundPatchEventMessage msg = buildMessage("VIEW", UUID.randomUUID(), UUID.randomUUID(), null);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(MetricEventType.VIEW);
    }

    @Test
    void mapearSkipALeave() {
        InboundPatchEventMessage msg = buildMessage("SKIP", UUID.randomUUID(), UUID.randomUUID(), null);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(MetricEventType.LEAVE);
    }

    @Test
    void mapearCreateCorrectamente() {
        InboundPatchEventMessage msg = buildMessage("CREATE", null, UUID.randomUUID(), PatchCategory.GAMING);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(MetricEventType.CREATE);
    }

    @Test
    void mapearDeleteCorrectamente() {
        InboundPatchEventMessage msg = buildMessage("DELETE", null, UUID.randomUUID(), null);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(MetricEventType.DELETE);
    }

    @Test
    void tipoDesconocidoProduceEventTypeNull() {
        InboundPatchEventMessage msg = buildMessage("UNKNOWN_TYPE", UUID.randomUUID(), UUID.randomUUID(), null);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getEventType()).isNull();
    }

    @Test
    void excepcionDelUseCaseEsCapturadaSinPropagarla() {
        InboundPatchEventMessage msg = buildMessage("JOIN", UUID.randomUUID(), UUID.randomUUID(), null);
        doThrow(new RuntimeException("DB error")).when(processMetricEventUseCase).process(any());

        adapter.onPatchEvent(msg);
    }

    @Test
    void payloadOmiteCamposNulos() {
        InboundPatchEventMessage msg = buildMessage("CREATE", null, null, null);

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getPayload()).doesNotContainKey("userId");
        assertThat(captor.getValue().getPayload()).doesNotContainKey("patchCategory");
    }

    @Test
    void generaEventIdCuandoMensajeNoLoTiene() {
        InboundPatchEventMessage msg = new InboundPatchEventMessage();
        msg.setEventId(null);
        msg.setSourceModule("M06");
        msg.setEventType("JOIN");
        msg.setUserId(UUID.randomUUID());
        msg.setEmittedAt(LocalDateTime.now());

        adapter.onPatchEvent(msg);

        ArgumentCaptor<MetricEvent> captor = ArgumentCaptor.forClass(MetricEvent.class);
        verify(processMetricEventUseCase).process(captor.capture());
        assertThat(captor.getValue().getEventId()).isNotNull();
    }

    private InboundPatchEventMessage buildMessage(String eventType, UUID userId, UUID patchId, PatchCategory category) {
        InboundPatchEventMessage msg = new InboundPatchEventMessage();
        msg.setEventId(UUID.randomUUID());
        msg.setSourceModule("M06");
        msg.setEventType(eventType);
        msg.setUserId(userId);
        msg.setPatchId(patchId);
        msg.setPatchCategory(category);
        msg.setCampusZone("CAFETERIA");
        msg.setEmittedAt(LocalDateTime.now());
        return msg;
    }
}
