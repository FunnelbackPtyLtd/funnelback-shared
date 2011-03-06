#!/bin/sh

echo '<?xml version="1.0" encoding="UTF-8" standalone="no" ?> '
echo '<PADRE_result_packet> '
echo '<details> '
echo '    <padre_version>FUNNELBACK_PADRE_10.1.0.30 64MDPLFS-VEC3-DNAMS2 (Web/Enterprise)</padre_version> '
echo '</details> '
echo '<error> '
echo '<usermsg>An error has occurred in the search system.</usermsg> '
echo '<adminmsg>[68] Indexes written in an incompatible format.'
echo 'check_bldinfo()</adminmsg> '
echo '</error> '
echo '</PADRE_result_packet>'

exit 68; 