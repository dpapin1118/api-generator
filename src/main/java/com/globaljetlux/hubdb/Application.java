package com.globaljetlux.hubdb;

import com.globaljetlux.hubdb.config.Config;
import com.globaljetlux.hubdb.config.Definition;
import com.globaljetlux.hubdb.model.ModelInfo;
import com.globaljetlux.hubdb.model.RecordInfo;
import com.globaljetlux.hubdb.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(Application.class);

    @Autowired
    private ConfigService configService;


    @Autowired
    private InformationService metaInfo;

    @Autowired
    private ModelBuilderService modelBuilder;

    @Autowired
    private DtoBuilderService dtoBuilder;

    @Autowired
    private SourceFileService sourceFile;

    @Autowired
    private MappingBuilderService mappingBuilder;

    @Autowired
    private NamingService namingService;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");

        Config conf = configService.getConfig();

        // String PROJECT_BASE = "/home/dcrespe/prj/api-generator/src/main/java";
        String PROJECT_BASE = conf.projectBase;

        for (Definition def : conf.definition ) {

            List<RecordInfo> fields = metaInfo.getInfo(def);

            // Generate a fields of fields for the model.
            List<ModelInfo> modelInfo = modelBuilder.generate(fields);

            // Generate a fields of fields for the DTOs
            Map<String, List<ModelInfo>> dtoModelList = dtoBuilder.generate(def, modelInfo);


            // Generate Model Java files
            String modelName = namingService.capitalize(def.model.name);
            sourceFile.generate(PROJECT_BASE, def.model.packageName, modelName, modelInfo);

            // Generate the DTOs Java files.
            for (var e : dtoModelList.entrySet()) {
                sourceFile.generate(PROJECT_BASE, def.dto.packageName, e.getKey(), e.getValue());
            }

            // Generate the field mapper class
            mappingBuilder.generate(def, PROJECT_BASE, def.mapper.packageName, modelName + "Mapper", modelInfo, dtoModelList);

        }

    }

}