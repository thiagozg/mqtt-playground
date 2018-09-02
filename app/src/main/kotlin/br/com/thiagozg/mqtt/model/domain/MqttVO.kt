package br.com.thiagozg.mqtt.model.domain

/*
{
	"status":"OK",
	"config": {
		"OEM":"casenio"
	}
}
 */
class MqttVO(
    var status: String,
    var config: ConfigVO
)

class ConfigVO(var oem: String)
