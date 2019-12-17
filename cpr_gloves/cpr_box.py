import I2C_LCD_driver
import RPi.GPIO as GPIO
import time
import requests
import json

#app lambda host
host = 'https://avgnoqx20f.execute-api.ap-northeast-2.amazonaws.com/default/IoT_Project'
#web lambda host
host2 = 'https://52mpnxgee9.execute-api.us-east-2.amazonaws.com/default/iot_project'
data = {'index':0}

mylcd = I2C_LCD_driver.lcd()

GPIO.setmode(GPIO.BOARD)
GPIO.setup(11, GPIO.IN, pull_up_down=GPIO.PUD_UP)

rightarrow_char = [
	[0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00],
	[0x00,0x00,0x00,0x00,0x1F,0x1F,0x1F,0x1F],
	[0x10,0x18,0x1C,0x1E,0x1F,0x1F,0x1F,0x1F],
	[0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x18],
	[0x1F,0x1F,0x1F,0x1F,0x00,0x00,0x00,0x00],
	[0x1F,0x1F,0x1F,0x1F,0x1E,0x1C,0x18,0x10],
	[0x18,0x10,0x00,0x00,0x00,0x00,0x00,0x00]
]

leftarrow_char = [
	[0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00],
	[0x00,0x00,0x00,0x00,0x1F,0x1F,0x1F,0x1F],
	[0x01,0x03,0x07,0x0F,0x1F,0x1F,0x1F,0x1F],
	[0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x03],
	[0x1F,0x1F,0x1F,0x1F,0x00,0x00,0x00,0x00],
	[0x1F,0x1F,0x1F,0x1F,0x0F,0x07,0x03,0x01],
	[0x03,0x01,0x00,0x00,0x00,0x00,0x00,0x00]
]

def right_arrow():
	mylcd.lcd_load_custom_chars(rightarrow_char)
	#line1
	mylcd.lcd_write(0x80)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(2)
	mylcd.lcd_write_char(3)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)
	#line2
	mylcd.lcd_write(0xC0)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(5)
	mylcd.lcd_write_char(6)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)

def left_arrow():
	mylcd.lcd_load_custom_chars(leftarrow_char)
	#line1
	mylcd.lcd_write(0x80)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(3)
	mylcd.lcd_write_char(2)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(1)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)
	#line2
	mylcd.lcd_write(0xC0)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(6)
	mylcd.lcd_write_char(5)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(4)
	mylcd.lcd_write_char(0)
	mylcd.lcd_write_char(0)

index = 0

while True:
	#get 'where' data from lambda server
	#'where' contains gps data
	response = requests.get(host, headers=None)
	str_data = json.dumps(response.json()['where'])
	dict_data = json.loads(json.loads(str_data))
	
	#read pi's 'onoff' data
	#'onoff' separates button is pushed or not
	for d in dict_data:
		if d['onoff'] == 1:
			index = d['index']
			break
		else:
			print("pi number " + str(d['index']) + " : " + str(d['onoff']))
	print("----------")
	
	#button is pushed => post emergency data to lambda
	if GPIO.input(11) == False:
		requests.post(host, data = json.dumps(data), headers=None)
		print("emergency message sent to 119")
	
	#button from another pi is pushed => show arrow on lcd
	if d['onoff'] == 1:		
		if index != 0:
			print("signal sent from another pi")
			right_arrow()
			time.sleep(30)
			mylcd.lcd_clear()
		else:
			requests.post(host, data = json.dumps(data), headers = None) #reset 'onoff'
		break
	else:
		time.sleep(1)
		continue

	time.sleep(2)
	
if index == 0:
	pastmsg=''
	while True:
		#get 'time' data from lambda server
		#'time' contains estimated arrival time
		response = requests.get(host, headers=None)
		str_msg=json.dumps(response.json()['time'])
		msg = json.loads(json.loads(str_msg))

		#'time' data is not changed => continue
		if pastmsg == msg[0]['time']:
			time.sleep(1)
			continue

		#'time' data is changed => show arrival time on lcd	
		else:
			mylcd.lcd_display_string("Will be arrived" ,1)
			mylcd.lcd_display_string("in " + msg[0]['time'],2)
			time.sleep(30)
			mylcd.lcd_clear()

		pastmsg=msg[0]
		requests.post(host, data = json.dumps({'time':'','time_index':0}), headers=None) #reset 'time'
		break
