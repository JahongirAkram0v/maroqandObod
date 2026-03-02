package uz.samtuit.maroqandObod.botService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.model.OrgState;
import uz.samtuit.maroqandObod.service.OrgService;

import java.util.List;
import java.util.Map;

import static uz.samtuit.maroqandObod.config.KeyboardNameConfig.LOCATION_ORG;
import static uz.samtuit.maroqandObod.model.OrgState.LOCATION;

@Component
@RequiredArgsConstructor
public class SingUpService {

    private final OrgService orgService;
    private final SendService sendService;

    public void handleOrgMessage(Org org, Message message) {
        OrgState orgState = org.getOrgState();
        switch (orgState) {
            case PHONE_NUMBER -> {
                if (message.hasContact()) {
                    String phoneNumber = message.getContact().getPhoneNumber();
                    org.setPhoneNumber(phoneNumber);
                    org.setOrgState(LOCATION);
                    orgService.save(org);
                    String locationText = "\uD83D\uDCCD Iltimos, joylashuv ma’lumotlaringizni yuboring.";
                    sendService.send(Utils.text(org.getChatId(), locationText,
                            List.of(List.of(Map.of("text", LOCATION_ORG,"request_location", true))
                    )), "sendMessage");
                }
            }
            case LOCATION -> {
                if (message.hasLocation()) {
                    org.setLatitude(message.getLocation().getLatitude());
                    org.setLongitude(message.getLocation().getLongitude());
                    org.setOrgState(OrgState.IMAGE);
                    orgService.save(org);
                    sendService.send(Utils.removeKeyboard(org.getChatId(), "\uD83D\uDCF8 Iltimos, konteynerlar joylashgan joydan rasm yuboring."), "sendMessage");
                }
            }
            case IMAGE -> {
                if (message.hasPhoto()) {
                    String fileId = message.getPhoto().getLast().getFileId();
                    org.setImageId(fileId);
                    org.setOrgState(OrgState.CONTAINER_COUNT);
                    orgService.save(org);
                    sendService.send(Utils.text(org.getChatId(), "\uD83D\uDCE6 Konteynerlar sonini yuboring.\n(masalan: 2)"), "sendMessage");
                }
            }
            case CONTAINER_COUNT -> {
                try {
                    int containerCount = Integer.parseInt(message.getText());
                    if (containerCount > 0) {
                        org.setContainerCount(containerCount);
                        org.setOrgState(OrgState.READY);
                        orgService.save(org);
                        sendService.send(Utils.org(org.getChatId(), "\uD83C\uDF89 Tabriklaymiz! Ro‘yxatdan o‘tish yakunlandi.", org.isFilled()), "sendMessage");
                    } else {
                        sendService.send(Utils.text(org.getChatId(), "kontaynerlar sonini to'g'ri yuboring"), "sendMessage");
                    }
                } catch (NumberFormatException e) {
                    sendService.send(Utils.text(org.getChatId(), "kontaynerlar sonini to'g'ri yuboring"), "sendMessage");
                }
            }
        }
    }
}
