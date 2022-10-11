#!/bin/sh

PRG="$0"

APP_HOME=`dirname "$PRG"`/..
# make it fully qulaified
APP_HOME=`cd "$APP_HOME" && pwd`

# Location of the java installation
# Specify the location of your java installation using JAVA_HOME, or specify the
# path to the "java" binary using JAVACMD
# (set JAVACMD to "auto" for automatic detection)
#JAVA_HOME=""
JAVACMD="auto"
JAVA_OPTS="$JAVA_OPTS -Xms256m -Xmx1024m"

# Detect the location of the java binary
if [ -z "$JAVACMD" ] || [ "$JAVACMD" = "auto" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  fi
fi

# Hm, we still do not know the location of the java binary
if [ ! -x "$JAVACMD" ] ; then
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
fi
# Stop here if no java installation is defined/found
if [ ! -x "$JAVACMD" ] ; then
  echo "ERROR: Configuration variable JAVA_HOME or JAVACMD is not defined correctly."
  echo "       (JAVA_HOME='$JAVAHOME', JAVACMD='$JAVACMD')"
  exit 1
fi

unset _LIBJARS
for i in ${APP_HOME}/lib/*.jar ; do
	if [ -z "$_LIBJARS" ]; then
    _LIBJARS=$i
	else
    _LIBJARS=${_LIBJARS}:$i
	fi
done

RUN_CMD="$JAVACMD \
	-classpath $_LIBJARS \
	$JAVA_OPTS \
	-Dlog4j.configurationFile=${APP_HOME}/conf/log4j2.xml \
	com.konantech.connector.Main"

$RUN_CMD "$@"