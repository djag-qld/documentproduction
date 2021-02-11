package au.gov.qld.bdm.documentproduction.document;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class InlineTemplateService {

    private final Configuration configuration;
    
    public InlineTemplateService() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_30);
    }

    public String template(String nameOfTemplate, String template, Object templateData) throws IOException, TemplateException {
        Template ftl = new Template(nameOfTemplate, new StringReader(template), configuration);
        StringWriter writer = new StringWriter();
        ftl.process(templateData, writer);
        return writer.toString();
    }

}
