package se.disabledsecurity.xkcd.fetcher.configuration;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TikaConfiguration {

    /**
     * Creates a Tika bean configured for image detection.
     * This bean can be injected into services that need content type detection.
     */
    @Bean
    public Tika tika(TikaConfig tikaConfig) {
        return new Tika(tikaConfig);
    }

    /**
     * Optional: Create a custom TikaConfig if you need more control
     * over the detection process.
     */
    @Bean
    public TikaConfig tikaConfig() {
        return TikaConfig.getDefaultConfig();
    }

    /**
     * Optional: Expose the detector separately if needed for advanced use cases.
     */
    @Bean
    public Detector detector(TikaConfig tikaConfig) {
        return tikaConfig.getDetector();
    }
}
