SET DATABASE SQL SYNTAX PGS TRUE;
SET DATABASE SQL SIZE FALSE;

-- changeset reidd:1

CREATE TABLE UserConnection (userId varchar(255) NOT NULL,
  providerId VARCHAR(255) NOT NULL,
  providerUserId VARCHAR(255),
  rank INT NOT NULL,
  displayName VARCHAR(255),
  profileUrl VARCHAR(512),
  imageUrl VARCHAR(512),
  accessToken VARCHAR(255) NOT NULL,
  secret VARCHAR(255),
  refreshToken VARCHAR(255),
  expireTime bigint,
  PRIMARY KEY (userId, providerId, providerUserId));
CREATE UNIQUE index UserConnectionRank ON UserConnection(userId, providerId, rank);

CREATE TABLE Account (id SERIAL NOT NULL,
            username VARCHAR UNIQUE,
            firstName VARCHAR NOT NULL,
            lastName VARCHAR NOT NULL,
            password VARCHAR DEFAULT '',
            PRIMARY KEY (id));

CREATE TABLE AccountRoles (
    accountId INT,
    role VARCHAR NOT NULL,
    FOREIGN KEY (accountId) REFERENCES Account(id)
);

CREATE TABLE Project (id SERIAL NOT NULL,
            owner INT,
            name VARCHAR NOT NULL,
            description TEXT NOT NULL,
            trialverseId INT,
            PRIMARY KEY (id),
            FOREIGN KEY(owner) REFERENCES Account(id));

CREATE TABLE Outcome (id SERIAL NOT NULL,
                      project INT,
                      name VARCHAR NOT NULL,
                      motivation TEXT NOT NULL,
                      semanticOutcomeLabel VARCHAR NOT NULL,
                      semanticOutcomeUri VARCHAR NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(project) REFERENCES Project(id));

CREATE TABLE Intervention (id SERIAL NOT NULL,
                           project INT,
                           name VARCHAR NOT NULL,
                           motivation TEXT NOT NULL,
                           semanticInterventionLabel VARCHAR NOT NULL,
                           semanticInterventionUri VARCHAR NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(project) REFERENCES Project(id));

CREATE TABLE Analysis (id SERIAL NOT NULL,
        projectId INT,
        name VARCHAR NOT NULL,
        analysisType VARCHAR NOT NULL,
        studyId INT,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id));

CREATE TABLE Analysis_Outcomes (
  AnalysisId INT,
  OutcomeId INT,
  PRIMARY KEY(AnalysisId, OutcomeId),
  FOREIGN KEY(AnalysisId) REFERENCES Analysis(id),
  FOREIGN KEY(OutcomeId) REFERENCES Outcome(id)
);

CREATE TABLE Analysis_Interventions (
  AnalysisId INT,
  InterventionId INT,
  PRIMARY KEY(AnalysisId, InterventionId),
  FOREIGN KEY(AnalysisId) REFERENCES Analysis(id),
  FOREIGN KEY(InterventionId) REFERENCES Intervention(id)
);

-- changeset reidd:2

CREATE TABLE Scenario (id SERIAL NOT NULL,
    						workspace INT NOT NULL,
    						title VARCHAR NOT NULL,
    						state VARCHAR NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(workspace) REFERENCES Analysis(id));

-- changeset reidd:3

ALTER TABLE Analysis ADD problem VARCHAR NULL;

-- changeset stroombergc:4
CREATE SEQUENCE shared_analysis_id_seq;

CREATE TABLE SingleStudyBenefitRiskAnalysis (id INT DEFAULT nextval('shared_analysis_id_seq') NOT NULL,
        projectId INT,
        name VARCHAR NOT NULL,
        studyId INT,
        problem VARCHAR NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id));

CREATE TABLE NetworkMetaAnalysis (id INT DEFAULT nextval('shared_analysis_id_seq') NOT NULL,
          projectId INT,
          name VARCHAR NOT NULL,
          studyId INT,
          outcomeId INT,
          problem VARCHAR NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(projectId) REFERENCES Project(id),
    FOREIGN KEY(outcomeId) REFERENCES Outcome(id));


DROP TABLE Analysis CASCADE;

ALTER TABLE Analysis_Outcomes RENAME TO SingleStudyBenefitRiskAnalysis_Outcomes;
ALTER TABLE Analysis_Interventions RENAME TO SingleStudyBenefitRiskAnalysis_Interventions;

ALTER TABLE SingleStudyBenefitRiskAnalysis_Interventions ADD CONSTRAINT ssbr_analysis_interventions_analysisid_fkey FOREIGN KEY (analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id);
ALTER TABLE SingleStudyBenefitRiskAnalysis_Outcomes ADD CONSTRAINT ssbr_analysis_outcomes_analysisid_fkey FOREIGN KEY (analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id);
ALTER TABLE scenario ADD CONSTRAINT ssbr_scenario_workspace_fkey FOREIGN KEY (workspace) REFERENCES SingleStudyBenefitRiskAnalysis(id);

-- changeset stroombergc:5
CREATE TABLE Model (
  id SERIAL NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id));

-- changeset reidd:6
CREATE TABLE ArmExclusion (
  id SERIAL NOT NULL,
  trialverseId BIGINT NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id)
);

-- changeset reidd:7
CREATE TABLE InterventionExclusion (
  id SERIAL NOT NULL,
  interventionId INT NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id),
  FOREIGN KEY(interventionId) REFERENCES Intervention(id)
);

-- changeset gertvv:8
ALTER TABLE NetworkMetaAnalysis DROP COLUMN studyId;
ALTER TABLE NetworkMetaAnalysis DROP COLUMN problem;

CREATE TABLE PataviTask (
  id SERIAL NOT NULL,
  modelId INT NOT NULL,
  method varchar,
  problem TEXT,
  result TEXT,
  PRIMARY KEY(id),
  FOREIGN KEY(modelId) REFERENCES Model(id)
);

-- changeset reidd:9
DROP TABLE InterventionExclusion CASCADE;

CREATE TABLE InterventionInclusion (
  id SERIAL NOT NULL,
  interventionId INT NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id),
  FOREIGN KEY(interventionId) REFERENCES Intervention(id)
);

--changeset reidd:10
ALTER TABLE Project DROP COLUMN trialverseId;
ALTER TABLE Project ADD COLUMN namespaceUid VARCHAR;

--changeset stroombergc:11
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP COLUMN studyId;
ALTER TABLE SingleStudyBenefitRiskAnalysis ADD COLUMN studyUid VARCHAR;

--changeset stroombergc:12
ALTER TABLE ArmExclusion DROP COLUMN trialverseId;
ALTER TABLE ArmExclusion ADD COLUMN trialverseUid VARCHAR;

--changeset reidd:13
CREATE TABLE remarks (
  analysisId INT NOT NULL,
  remarks TEXT,
  PRIMARY KEY(analysisId),
  FOREIGN KEY(analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id)
);

--changeset stroombergc:14
ALTER TABLE project ADD COLUMN datasetVersion VARCHAR;
--rollback ALTER TABLE project drop column datasetVersion

--changeset stroombergc:15
-- this can not be done in hsql, this is a problem
--ALTER TABLE project ALTER column description DROP NOT NULL;
--rollback ALTER TABLE project ALTER COLUMN description SET NOT NULL;

--changeset reidd:16
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP COLUMN studyUId;
ALTER TABLE SingleStudyBenefitRiskAnalysis ADD COLUMN studyGraphUid VARCHAR;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis DROP COLUMN studyGraphUid;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis ADD COLUMN studyUid VARCHAR;

--changeset stroombergc:17
DROP TABLE IF EXISTS PataviTask;
ALTER TABLE model ADD COLUMN taskId INT;

--changeset stroombergc:18
ALTER TABLE model ADD COLUMN title VARCHAR NOT NULL DEFAULT 'model 1 (generated by conversion)';

--changeset reidd:19
ALTER TABLE model ADD COLUMN linearModel VARCHAR NOT NULL DEFAULT 'fixed';
--rollback ALTER TABLE model DROP COLUMN linearModel ;

--changeset stroombergc:20
ALTER TABLE model ADD COLUMN modelType VARCHAR NOT NULL DEFAULT '{"type": "network"}';
--rollback ALTER TABLE model DROP COLUMN modelType ;

--changeset stroombergc:21
ALTER TABLE model ALTER COLUMN linearModel SET DEFAULT 'random';
--rollback ALTER TABLE model ALTER COLUMN linearModel SET DEFAULT 'fixed';

--changeset reidd:22
ALTER TABLE model ADD COLUMN burnInIterations INT NOT NULL DEFAULT 5000;
ALTER TABLE model ADD COLUMN inferenceIterations INT NOT NULL DEFAULT 20000;
ALTER TABLE model ADD COLUMN thinningFactor INT NOT NULL DEFAULT 10;
--rollback ALTER TABLE model DROP COLUMN burnInIterations;
--rollback ALTER TABLE model DROP COLUMN inferenceIterations;
--rollback ALTER TABLE model DROP COLUMN thinningFactor;

--changeset stroombergc:23
ALTER TABLE model ADD COLUMN likelihood VARCHAR(255);
ALTER TABLE model ADD COLUMN link VARCHAR(255);
ALTER TABLE model ALTER likelihood SET NOT NULL;
ALTER TABLE model ALTER link SET NOT NULL;
--rollback ALTER TABLE model DROP COLUMN likelihood;
--rollback ALTER TABLE model DROP COLUMN link;

--changeset stroombergc:24
ALTER TABLE model ADD COLUMN outcomeScale DOUBLE PRECISION;
--rollback ALTER TABLE model DROP COLUMN outcomeScale;

--changeset stroombergc:25
ALTER TABLE SingleStudyBenefitRiskAnalysis ALTER COLUMN name RENAME TO title;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis ALTER COLUMN title RENAME TO name;
ALTER TABLE NetworkMetaAnalysis ALTER COLUMN name RENAME TO title;
--rollback ALTER TABLE NetworkMetaAnalysis ALTER COLUMN title RENAME TO name;

--changeset stroomberg 26:
ALTER TABLE Account ADD COLUMN email VARCHAR(255);
--rollback ALTER TABLE Account DROP COLUMN email;

--changeset reidd:27
ALTER TABLE model ADD COLUMN heterogeneityPrior VARCHAR;
--rollback ALTER TABLE model DROP COLUMN heterogeneityPrior;
