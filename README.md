# AI Analysis Plugin for Jenkins

The **AI Analysis Plugin** is a Jenkins plugin that provides automated AI-driven analysis of build logs. It scans your build logs for errors and exceptions, sends each detected issue to an LLM service for analysis, and displays the results on the build’s sidebar. This plugin is configurable on a per-job basis, allowing you to opt in, specify the LLM service URL, and select a Jenkins credentials entry for secure token management.

## Features

- **Per-Job Configuration:**  
  Enable or disable AI Analysis per job, set the LLM URL, and specify the credentials ID for the token.
- **Automated Log Analysis:**  
  Scans build logs for lines containing "error" or "exception" and sends each issue to the LLM service.
- **Sidebar Integration:**  
  Adds an “AI Analysis” link to the build page’s sidebar if the plugin is enabled.
- **Secure Token Management:**  
  Retrieves the LLM token securely from Jenkins Credentials.
- **CI/CD Integration:**  
  Fully automated build, integration testing, and release via GitHub Actions.

## Prerequisites

- **Jenkins:** Version 2.346.1 or later.
- **Jenkins Credentials Plugin:** To manage secret text credentials.
- **JDK 8:** (or later) is required to compile and run the plugin.
- **Maven:** For building the plugin.

## Installation

1. **Build the Plugin:**

   Clone the repository and run:

   ```bash
   mvn clean package
   ```
   his will generate the HPI file (e.g., `target/aianalysis-plugin.hpi`).

1. **Install on Jenkins:**

    - Log in to your Jenkins instance as an administrator.
    - Go to **Manage Jenkins → Manage Plugins → Advanced**.
    - Under **Upload Plugin**, choose the HPI file and click Upload.
    - Restart Jenkins if required.

## Configuration
### Job Property Configuration
When configuring a job (either freestyle or Pipeline), you can opt in for AI Analysis:

1. **Freestyle Projects:**

    In the job’s configuration page, check the Enable AI Analysis option, enter your LLM service URL, and provide the Credentials ID.
2. **Pipeline Projects:**

    You can enable the property via your Jenkinsfile using the `properties` step:

    ```groovy
    properties([
    [$class: 'AIAnalysisJobProperty',
    enabled: true,
    llmUrl: 'https://your-llm-server.com/api/v1',
    credentialsId: 'your-llm-token-id']
    ])
    ```
## Usage
After enabling the plugin for a job, run a build.</br>
Upon completion, an AI Analysis link will appear in the build’s sidebar.</br>
Clicking the link displays a page with the detected error/exception lines and the corresponding AI analysis results.

## Development
### Repository Structure
``` bash
aianalysis-plugin/
├── .github
│   ├── workflows   
│   │   ├── ci-cd.yml
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/adiroth/jenkins/plugins/aianalysis/
│   │   │       ├── AIAnalysisBuildAction.java
│   │   │       ├── AIAnalysisJobProperty.java
│   │   │       └── LLMService.java
│   │   └── resources
│   │       ├── com/adiroth/jenkins/plugins/aianalysis/AIAnalysisJobProperty/
│   │       │   └── config.jelly
│   │       ├── hudson/model/Run/aiAnalysis.jelly
│   │       └── images/
│   │           └── ai-icon.png
└── src
    └── test
        └── java/tests.java
```
## Building and Debugging with VSCode
1. **Set Up VSCode:**
    - Install the **Extension Pack for Java, Maven for Java**, and **Debugger for Java**.
    - Open the repository folder in VSCode.
2. **VSCode Tasks and Debug Configuration:**
    Use the provided `.vscode/tasks.json` and `.vscode/launch.json` for packaging and debugging.
3. **Local Testing:**
    Run the Jenkins instance locally with:
    ``` bash
    mvn hpi:run -Djava.debug=5005
    ```
    Then attach the VSCode debugger to port `5005` using your launch configuration.
## CI/CD with GitHub Actions
This repository uses GitHub Actions to automate the following:
- **Build, Lint, and Test:**
    Runs Maven commands to verify the code quality.
- **Integration Testing:**
Launches a Jenkins emulator with the plugin and verifies that it is registered.
- **Release Automation:**
Generates a changelog based on the commit diff since the latest tag, creates a GitHub release, and attaches the HPI file as a release asset.

See the [.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml) file for the full workflow configuration.
## Contributing
Contributions are welcome! Please open issues or submit pull requests for any bugs, enhancements, or new features.
## License
This project is licensed under the MIT [License](License). See the LICENSE file for details.