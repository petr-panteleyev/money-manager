#!/bin/sh

if [ -z "$1" ]
then
  echo "Usage: sudo -E install.sh <install dir>"
  exit
fi

LAUNCH_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
INSTALL_DIR=$1/money-manager

echo "Installing into $INSTALL_DIR..."

mkdir -p $INSTALL_DIR/jars

rm -f $INSTALL_DIR/jars/*
cp -f $LAUNCH_DIR/../desktop/app/target/jmods/* $INSTALL_DIR/jars
cp -f $LAUNCH_DIR/../desktop/icons/icon.png $INSTALL_DIR

echo "
#!/bin/sh
$JAVA_HOME/bin/java --module-path $INSTALL_DIR/jars \\
  --add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls \\
  --module org.panteleyev.money/org.panteleyev.money.MoneyApplication
" > $INSTALL_DIR/money-manager.sh

chmod +x $INSTALL_DIR/money-manager.sh

echo "[Desktop Entry]
Type=Application
Version=1.5
Name=Money Manager
Name[ru_RU]=Менеджер финансов
Comment=Application to manage personal finances
Comment[ru_RU]=Программа для управления личными финансами
Icon=$INSTALL_DIR/icon.png
Exec=/bin/sh $INSTALL_DIR/money-manager.sh
Categories=Office;Finance;Java;
" > $INSTALL_DIR/money-manager.desktop
