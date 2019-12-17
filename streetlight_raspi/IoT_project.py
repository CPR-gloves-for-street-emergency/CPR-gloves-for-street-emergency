#!/usr/bin/python
# -*- coding: utf-8 -*-

import smbus
import math
import time 
import spidev
import json
import requests
import urllib2
from threading import Thread
import pygame

power_mgmt_1 = 0x6b
power_mgmt_2 = 0x6c

data = {"id": 2, "force": 0, "gyro": 0, }

host='https://52mpnxgee9.execute-api.us-east-2.amazonaws.com/default/iot_project'

naver_client_id = "ob7ynszadh"
naver_client_secret = "uXo9uqoRrQ8QTp0upLN7NVJWMxNKoINvp10spZ5G"

def read_byte(adr):

    return bus.read_byte_data(address, adr)

 
def read_word(adr):

    high = bus.read_byte_data(address, adr)
    low = bus.read_byte_data(address, adr+1)
    val = (high << 8) + low 

    return val

def read_word_2c(adr):

    val = read_word(adr) 

    if (val >= 0x8000):
        return -((65535 - val) + 1)
    else:
        return val

def dist(a,b):
    return math.sqrt((a*a)+(b*b))

def get_y_rotation(x,y,z):
    radians = math.atan2(x, dist(y,z))
    return -math.degrees(radians)

def analog_read(channel):
	r = spi.xfer2([1, (8 + channel) << 4, 0])
	adc_out = ((r[1]&3) << 8) + r[2]
	return adc_out
	
def naverTTS(text, outputfile):
    print("[naver] tts is started")
    before = int(round(time.time() * 1000))
    encText = urllib2.quote(text)
    data = "speaker=clara&speed=0&text=" + encText;
    url = "https://naveropenapi.apigw.ntruss.com/voice/v1/tts"
    request = urllib2.Request(url)
    request.add_header("X-NCP-APIGW-API-KEY-ID", naver_client_id)
    request.add_header("X-NCP-APIGW-API-KEY", naver_client_secret)
    response = urllib2.urlopen(request, data=data.encode('utf-8'))
    rescode = response.getcode()
    if rescode == 200:
        response_body = response.read()
        with open(outputfile, 'wb') as f:
            f.write(response_body)
    else:
        print("Error Code:" + rescode)

    now = int(round(time.time() * 1000)) - before
    print("[naver] tts is finished - " + str(now) + "\n")
    print("-" * 10)


bus = smbus.SMBus(1) 
address = 0x68       
bus.write_byte_data(address, power_mgmt_1, 0)

spi = spidev.SpiDev()
spi.open(0, 0)
spi.max_speed_hz = 1350000

def post_sensor_val():
	while True:
		reading = analog_read(0)
		voltage = reading * 3.3 / 1024
		#print('force reading = %d	voltage = %f' % (reading, voltage))

		accel_xout = read_word_2c(0x3b)
		accel_yout = read_word_2c(0x3d)
		accel_zout = read_word_2c(0x3f)
		accel_xout_scaled = accel_xout / 16384.0
		accel_yout_scaled = accel_yout / 16384.0
		accel_zout_scaled = accel_zout / 16384.0
		y_rota = int(get_y_rotation(accel_xout_scaled, accel_yout_scaled, accel_zout_scaled))
		#print "y rotation: " , y_rota
		
		data['force'] = reading
		data['gyro'] = y_rota		
		#print('data : ' , data)

		post_response = requests.post(host, data=json.dumps(data), headers=None)
		get_response = requests.get(host, headers=None)
		
		print('post : ' + post_response.json()['body'])
		print('get : ' + get_response.json()['body'])


		time.sleep(0.4)
	

def get_emergency_msg():
	msg_buffer = ""
	while True:		
		tts_text = ""
		file_name = './output.mp3'
		
		response = requests.get(host, headers=None)
		#print('emergency_msg : ' + response.json()['emer'])
		temp_msg = json.loads(response.json()['emer'])
		emergency_msg = str(temp_msg['emergency_msg'])
		if (emergency_msg == "") or (emergency_msg == msg_buffer):
			time.sleep(1)
			continue;
		else :
			msg_buffer = emergency_msg
			tts_text = emergency_msg
			
			naverTTS(tts_text, file_name)

			time.sleep(3)

			tts = "output.mp3"

			pygame.mixer.init(16000, -16, 1, 2048)
			pygame.mixer.music.load(tts)
			pygame.mixer.music.play()

			clock = pygame.time.Clock()
			while pygame.mixer.music.get_busy():
				clock.tick(30)
			pygame.mixer.quit()
			
			time.sleep(1)
			

t1 = Thread(target=post_sensor_val)
t1.start()
t2 = Thread(target=get_emergency_msg)
t2.start()
