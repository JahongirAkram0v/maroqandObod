package uz.samtuit.maroqandObod.botService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.service.OrgService;

@Component
@RequiredArgsConstructor
public class SingUpService {

    private final OrgService orgService;

    public void checkAndHandleOrgMessage(Org org, Message message) {
        if (org.getPhoneNumber() == null) {
            if (!message.hasContact()) {
                //show error and repeat request for phone number
                return;
            }
            String phoneNumber = message.getContact().getPhoneNumber();
            org.setPhoneNumber(phoneNumber);
            orgService.save(org);
            //
            if (org.getLatitude() == null) {
                //show request for location
            }
            else if (org.getImageId() == null) {
                // show request for image
            }
            else if (org.getContainerCount() == 0) {
                // show request for container count
            }
        }
        else if (org.getLatitude() == null) {
            if (!message.hasLocation()) {
                // show error and repeat request for location
                return;
            }
            org.setLatitude(message.getLocation().getLatitude());
            org.setLongitude(message.getLocation().getLongitude());
            orgService.save(org);
            //
            if (org.getImageId() == null) {
                // show request for image
            }
            else if (org.getContainerCount() == 0) {
                // show request for container count
            }
        }
        else if (org.getImageId() == null) {
            if (!message.hasPhoto()) {
                // show error and repeat request for image
                return;
            }
            String fileId = message.getPhoto().getLast().getFileId();
            org.setImageId(fileId);
            orgService.save(org);
            //
            if (org.getContainerCount() == 0) {
                // show request for container count
            }
        }
         else if (org.getContainerCount() == 0) {
             try {
                 int containerCount = Integer.parseInt(message.getText());
                 if (containerCount <= 0) {
                     // show error and repeat request for container count
                     return;
                 }
                 org.setContainerCount(containerCount);
                 org.setFilled(true);
                 orgService.save(org);
             } catch (NumberFormatException e) {
                 // show error and repeat request for container count
             }
        }
    }
}
