package SimsLiteTelegramBot;

import SimsLiteTelegramBot.Types.GenderType;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;

public class Utils {
    public static String GetPronounceByGender(String gender) {
        if (gender.equals(GenderType.Male)) {
            return "он";
        } else {
            return "она";
        }
    }

    public SendPhoto GetSendPhotoMessage(String caption, String chatId, String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();

        var inputFile = new InputFile();
        inputFile.setMedia(new File(classLoader.getResource(fileName).getFile()), fileName);

        var sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setChatId(chatId);
        return sendPhoto;
    }

    //    public SendMediaGroup SendMediaGroupWithPhotos(String caption, String chatId, ArrayList<String> fileNames) {
    //        var sendMediaGroup = new SendMediaGroup();
    //        var inputMedias = new ArrayList<InputMedia>();
    //        ClassLoader classLoader = getClass().getClassLoader();
    //
    //        for (String fileName : fileNames) {
    //            var inputMediaPhoto = new InputMediaPhoto();
    //            inputMediaPhoto.setMedia(new File(classLoader.getResource(fileName).getFile()), fileName);
    //            inputMedias.add(inputMediaPhoto);
    //        }
    //
    //        inputMedias.get(0).setCaption(caption);
    //        sendMediaGroup.setChatId(chatId);
    //        sendMediaGroup.setMedias(inputMedias);
    //        return sendMediaGroup;
    //    }
}
