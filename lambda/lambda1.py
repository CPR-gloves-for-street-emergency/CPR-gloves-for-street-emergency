import json

pos = [{"index": 0, "x": 37.284839, "y": 127.045262, "onoff": 0},
	{"index" : 1, "x": 37.279691, "y": 127.043608, "onoff": 0}]
arrival_time = [{"index": 1, "time": ''},
	{"index": 1, "time": ''}]

def lambda_handler(event, context):
	#TODO implement

	global pos
	global arrival_time

	if 'index' in event:
		pos[event['index']]['onoff'] = 1 - pos[event['index']]['onoff']
	if 'time' in event :
		arrival_time[event['time_index']]['time']=event['time']
	return {
		'statusCode': 200,
		'where':json.dumps(pos),
		'time':json.dumps(arrival_time)
	}