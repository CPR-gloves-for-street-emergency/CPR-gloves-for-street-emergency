import json

sensor_val = {'force' : 0, 'gyro': 0, 'isCurrent':1}
msg = {'emergency_msg': ""}

def lambda_handler(event, context):
    # TODO implement

    global sensor_val
    global msg
    
    if ('gyro' in event) and ('force' in event):
        sensor_val = event
    elif ('emergency_msg' in event) and (event['emergency_msg'] != ""):
        msg = event
    print(sensor_val)
        
    return {
        'statusCode': 200,
        'body': json.dumps(sensor_val),
        'emer': json.dumps(msg)
    }
