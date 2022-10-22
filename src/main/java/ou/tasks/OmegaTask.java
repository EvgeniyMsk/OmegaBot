package ou.tasks;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import ou.bot.OmegaBot;
import ou.entities.Car;
import ou.entities.TextRequest;

import java.io.File;
import java.io.PrintWriter;

@Slf4j
public class OmegaTask implements Runnable {
    private final OmegaBot omegaBot;
    private int counter = 0;
    private final String taskId;
    private final Update update;

    private TextRequest request;

    public OmegaTask(OmegaBot omegaBot,
                     String taskId,
                     Update update,
                     TextRequest request) {
        this.omegaBot = omegaBot;
        this.taskId = taskId;
        this.update = update;
        this.request = request;
    }

    @Override
    public void run() {
        try {
            PrintWriter printWriter = new PrintWriter(this.omegaBot.getPath() + taskId + ".txt");
            while (counter < 10) {
                try {
                    Thread.sleep(1000);
                    counter++;
                    String type = request instanceof Car ? "Car" : "Person";
                    printWriter.println(String.format("Counter: %d, Task:%s, Type:%s", counter, taskId, type));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            printWriter.close();
            InputFile file = new InputFile(new File(this.omegaBot.getPath() + taskId + ".txt"));
            SendDocument sendDocument = new SendDocument();
            sendDocument.setDocument(file);
            sendDocument.setChatId(this.update.getMessage().getChatId().toString());
            this.omegaBot.execute(sendDocument);
            this.omegaBot.removeId(update.getMessage().getFrom().getId());

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public int getCounter() {
        return counter;
    }

    public String getTaskId() {
        return taskId;
    }
}

