#!/bin/bash
mvn -DaltDeploymentRepository=snapshots-repo::default::file:./snapshots clean deploy
