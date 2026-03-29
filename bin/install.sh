#!/bin/sh

# Copyright © 2026 Petr Panteleyev
# SPDX-License-Identifier: BSD-2-Clause
if [ -z "$1" ]
then
  echo "Usage: install.sh <install dir>"
  exit
fi

LAUNCH_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
INSTALL_DIR=$(realpath -m "$1")/money-manager

echo -n "Removing $INSTALL_DIR... "
rm -rf $INSTALL_DIR
echo "done"

echo -n "Creating $INSTALL_DIR... "
mkdir -p $INSTALL_DIR
echo "done"

echo -n "Installing into $INSTALL_DIR... "
cp -r $LAUNCH_DIR/../desktop/app/target/dist/Money\ Manager/* $INSTALL_DIR
echo "done"

echo -n "Creating desktop entry... "
echo "[Desktop Entry]
Type=Application
Version=1.5
Name=Money Manager
Name[ru_RU]=Менеджер финансов
Comment=Application to manage personal finances
Comment[ru_RU]=Программа для управления личными финансами
Icon=$INSTALL_DIR/lib/Money\sManager.png
Exec=\"$INSTALL_DIR/bin/Money Manager\"
Categories=Office;Finance;Java;
" > $HOME/.local/share/applications/money-manager.desktop
echo "done"
