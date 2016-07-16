# Amazon S3 Bucket Backup Rotator

A simple backup rotation software for managing daily backups. Use at own risk, this program deletes backups!

This program keeps

1. One Backup per Week.
1. One Backup per Month.
1. Additionally One Copy Per Year.

All other backups will be deleted!

## How It Works

Once executed it will rotate all files found inside a daily bucket and move them to a weekly bucket. Then Files inside the weekly bucket will be moved to the monthly bucket. And files inside the monthly bucket will be copied to the yearly bucket.

You will keep all daily backups for a full week and weekly backups for a full month.

1. Backups of the current week will not be touched!
2. Backups of the current month will not be touched!

## The File Collector

Files must have a date pattern inside their filenames! All files that have a date inside their filename will be collected and rotated.

The default date format must be like this: 2016-05-31-00-15-02

The technical java date format is this: `yyyy-MM-dd-HH-mm-ss`

The resulting filename can be something like this: JenkinsBackupV1-2016-05-31-00-15-02.tar.gz.gp

### Setting The Filename Format

You can change the filename format with the configuration variable BACKUPROTATOR_DATE_PATTERN. This variable must be a regular expression.

The default value is: `(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})`

### Setting The Date format

You can change the date format with the configuration variable BACKUPROTATOR_DATE_FORMAT. This variable must be a java date format.

The default value is: `yyyy-MM-dd-HH-mm-ss`

## Simulation Mode

This program has a simulation mode where files are not touched only reported. The simulation mode is set by the variable BACKUPROTATOR_SIMULATION_MODE.

The variable must be either `true` or `false`.

Output Example:

~~~~
MOVE NginxBackupV2-2016-06-10-00-30-01.tar.gz.gpg FROM /backupsink/daily TO /backupsink/weekly
MOVE NginxBackupV2-2016-07-09-00-30-02.tar.gz.gpg FROM /backupsink/daily TO /backupsink/weekly
MOVE NginxBackupV2-2016-05-28-00-30-00.tar.gz.gpg FROM /backupsink/daily TO /backupsink/weekly
DELETE NginxBackupV2-2016-06-07-00-30-00.tar.gz.gpg FROM /backupsink/daily
DELETE NginxBackupV2-2016-06-23-00-30-01.tar.gz.gpg FROM /backupsink/daily
DELETE NginxBackupV2-2016-06-17-00-30-00.tar.gz.gpg FROM /backupsink/daily
MOVE NginxBackupV2-2016-05-14-00-30-01.tar.gz.gpg FROM /backupsink/daily TO /backupsink/weekly
~~~~

## Configuration

This program is configured by environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| BACKUPROTATOR_SIMULATION_MODE | `true` or `false` For Simulation | true | |
| BACKUPROTATOR_AWS_ACCESS_KEY | AWS Access Key | true |  |
| BACKUPROTATOR_AWS_SECRET_KEY | AWS Secret Key | true |  |
| BACKUPROTATOR_AWS_REGION | AWS Region | true | |
| BACKUPROTATOR_DAILY_BUCKET_NAME | Bucket Name | false | `backups` |
| BACKUPROTATOR_DAILY_BUCKET_PATH | Path | false | `daily` |
| BACKUPROTATOR_WEEKLY_BUCKET_NAME | Bucket Name | false | `backups` |
| BACKUPROTATOR_WEEKLY_BUCKET_PATH | Path | false | `weekly`|
| BACKUPROTATOR_MONTHLY_BUCKET_NAME | Bucket Name | false | `backups` |
| BACKUPROTATOR_MONTHLY_BUCKET_PATH | Path | false | `monthly` |
| BACKUPROTATOR_YEARLY_BUCKET_NAME | Bucket Name | false | `backups` |
| BACKUPROTATOR_YEARLY_BUCKET_PATH | Path | false | `yearly` |
| BACKUPROTATOR_DATE_PATTERN | Regular Expression Date Pattern | false | `(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})` |
| BACKUPROTATOR_DATE_FORMAT | Java Date Format | false | `yyyy-MM-dd-HH-mm-ss` |

## Building

~~~~
$ gradle build
~~~~

## Building Fat Jar

~~~~
$ gradle fatJar
~~~~

## Running Docker Builder

Running in build container

~~~~
$ docker run -it --rm -v $(pwd):/workspace blacklabelops/swarm-jdk8 bash
$ cd /workspace
$ gradle fatJar
~~~~

## Running Java Environment

~~~~
$ docker run -it --rm -v $(pwd)/build/libs/backrotator-all.jar:/backrotator-all.jar blacklabelops/java bash
$ java -jar /backrotator-all.jar
~~~~

Starting with log4j configuration

~~~~
$ java -Dlog4j.configuration=file:/build/libs/log4j.properties -jar backrotator-all.jar
~~~~

## Running With Minimum Environment Variables

~~~~
$ docker run \
    -e 'BACKUPROTATOR_SIMULATION_MODE=true' \
    -e 'BACKUPROTATOR_AWS_ACCESS_KEY=YOUR_KEY' \
    -e 'BACKUPROTATOR_AWS_SECRET_KEY=YOUR_SECRET_KEY' \
    -e 'BACKUPROTATOR_AWS_REGION=eu-central-1' \
    -it --rm -v $(pwd)/build/libs/backrotator-all.jar:/backrotator-all.jar blacklabelops/java bash
$ java -jar /backrotator-all.jar
~~~~


## Running With All Environment Variables

~~~~
$ docker run \
    -e 'BACKUPROTATOR_DAILY_BUCKET_NAME=backupsink' \
    -e 'BACKUPROTATOR_WEEKLY_BUCKET_NAME=backupsink' \
    -e 'BACKUPROTATOR_MONTHLY_BUCKET_NAME=backupsink' \
    -e 'BACKUPROTATOR_YEARLY_BUCKET_NAME=backupsink' \
    -e 'BACKUPROTATOR_DAILY_BUCKET_PATH=dailies' \
    -e 'BACKUPROTATOR_WEEKLY_BUCKET_PATH=weekly' \
    -e 'BACKUPROTATOR_MONTHLY_BUCKET_PATH=monthly' \
    -e 'BACKUPROTATOR_YEARLY_BUCKET_PATH=yearly' \
    -e 'BACKUPROTATOR_SIMULATION_MODE=true' \
    -e 'BACKUPROTATOR_AWS_ACCESS_KEY=YOUR_KEY' \
    -e 'BACKUPROTATOR_AWS_SECRET_KEY=YOUR_SECRET_KEY' \
    -e 'BACKUPROTATOR_AWS_REGION=eu-central-1' \
    -e 'BACKUPROTATOR_DATE_PATTERN=(\d{4})-(\d{2})-(\d{2})-(\d{2})-(\d{2})-(\d{2})' \
    -e 'BACKUPROTATOR_DATE_FORMAT=yyyy-MM-dd-HH-mm-ss' \
    -it --rm -v $(pwd)/build/libs/backrotator-all.jar:/backrotator-all.jar blacklabelops/java bash
$ java -jar /backrotator-all.jar
~~~~
