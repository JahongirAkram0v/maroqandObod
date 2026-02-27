package uz.samtuit.maroqandObod.botService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.model.OrgState;
import uz.samtuit.maroqandObod.service.OrgService;

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
                    org.setOrgState(OrgState.LOCATION);
                    orgService.save(org);
                    sendService.send(Utils.location(org.getChatId(), "joylashuv ma'lumotlarini kiriting"), "sendMessage");
                }
            }
            case LOCATION -> {
                if (message.hasLocation()) {
                    org.setLatitude(message.getLocation().getLatitude());
                    org.setLongitude(message.getLocation().getLongitude());
                    org.setOrgState(OrgState.IMAGE);
                    orgService.save(org);
                    sendService.send(Utils.text(org.getChatId(), "rasm yuboring"), "sendMessage");
                }
            }
            case IMAGE -> {
                if (message.hasPhoto()) {
                    String fileId = message.getPhoto().getLast().getFileId();
                    org.setImageId(fileId);
                    org.setOrgState(OrgState.CONTAINER_COUNT);
                    orgService.save(org);
                    sendService.send(Utils.text(org.getChatId(), "kontaynerlar sonini yuboring"), "sendMessage");
                }
            }
            case CONTAINER_COUNT -> {
                try {
                    int containerCount = Integer.parseInt(message.getText());
                    if (containerCount > 0) {
                        org.setContainerCount(containerCount);
                        org.setOrgState(OrgState.READY);
                        orgService.save(org);
                        sendService.send(Utils.orgKeyboard(org.getChatId(), "ro'yxatdan o'tdingiz", org.isFilled()), "sendMessage");
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
