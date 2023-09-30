import org.dplevine.patterns.pipeline.Pipeline;
import org.dplevine.patterns.pipeline.PipelineBuilder;

public class PipelineTest {

    public static void main(String args[]) {
        try {
            Pipeline pipeline = PipelineBuilder.createBuilder().buildFromPathName("/Users/dplevine/Desktop/example.yaml");
            pipeline.render("/Users/dplevine/Desktop/example", Pipeline.ImageType.GIF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}