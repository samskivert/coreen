#!/bin/sh
#
# $Id$
#
# Completes the installation of the Coreen app.

echo
echo "---------------------------------"
echo " Welcome to the Coreen installer!"
echo "---------------------------------"

# ask them which java installation to use
DEFJAVADIR=$JAVA_HOME
if [ ! -x $DEFJAVADIR/bin/java ]; then
    JAVABIN=`which java`
    if [ -x $JAVABIN ]; then
        DEFJAVADIR=`echo $JAVABIN | sed 's:/bin/java::g'`
    fi
fi
JAVADIR=
while [ -z "$JAVADIR" ]; do
    echo
    echo "Which Java Virtual Machine would you like to use?"
    echo "Note: the JVM must be version 1.6.0 or newer."
    echo -n "[$DEFJAVADIR] "
    read REPLY
    if [ -z "$REPLY" ]; then
        REPLY=$DEFJAVADIR
    fi
    if [ \! -x $REPLY/bin/java ]; then
        echo "Could not locate '$REPLY/bin/java'."
        echo "Please ensure that you entered the proper path."
    else
        JAVADIR=$REPLY
    fi
done

# ask them where they want to install the app
DEFINSTALLDIR=$HOME/coreen
INSTALLDIR=
while [ -z "$INSTALLDIR" ]; do
    echo
    echo "Where would you like to install Coreen?"
    echo -n "[$DEFINSTALLDIR] "
    read REPLY
    if [ -z "$REPLY" ]; then
        REPLY=$DEFINSTALLDIR
    fi
    if [ \! -d $REPLY ]; then
        echo "Creating directory '$REPLY'..."
        mkdir -p $REPLY
        if [ \! -d $REPLY ]; then
            echo "Unable to create directory '$REPLY'."
        else
            INSTALLDIR=$REPLY
            break
        fi
    else
        INSTALLDIR=$REPLY
        break
    fi
done

# copy our files to the install directory
cp -p * $INSTALLDIR
rm $INSTALLDIR/finish_install.sh

# set up the symlink pointing to the desired java installation
rm -f  $INSTALLDIR/java
ln -s $JAVADIR $INSTALLDIR/java

# attempt to locate their desktop directory
DESKTOP=$HOME/Desktop
if [ \! -d $DESKTOP ]; then
    DESKTOP=$HOME/.desktop
fi
if [ \! -d $DESKTOP ]; then
    DESKTOP=$INSTALLDIR
    echo
    echo "Note: Unable to locate your desktop directory. Please move"
    echo "'$DESKTOP/Coreen.desktop' to your desktop"
    echo "directory if you wish to launch Coreen from a desktop icon."
fi

cat > "$DESKTOP/Coreen.desktop" <<EOF
#!/usr/bin/env xdg-open
[Desktop Entry]
Name=Coreen
Exec=$INSTALLDIR/coreen
Icon=$INSTALLDIR/desktop.png
Terminal=false
MultipleArgs=false
Type=Application
Categories=Application;
EOF
chmod a+rx $DESKTOP/Coreen.desktop

echo
echo "--------------------------------------------------------"
echo "Coreen has been successfully installed!"
echo "Use $INSTALLDIR/coreen or the desktop icon to run it."
echo
echo "If you wish to uninstall Coreen later, simply delete the"
echo "$INSTALLDIR directory."
echo "--------------------------------------------------------"
