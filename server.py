from flask import Flask,request
import json
import mysql.connector
import sys
import time
import calendar

app = Flask(__name__)

@app.route('/attendance',methods = ['POST', 'GET'])
def attendance():
    if request.method == 'POST':
        print("请求的模块：",request.form['method'])
        #获取数据包
        data = json.loads(request.form['data'])
        #加载数据库
        mydb = mysql.connector.connect(
          host="localhost",
          user="root",
          passwd="*****",
          database="attendance"
        )
        mycursor = mydb.cursor()
        if request.form['method'] == 'register':
            #注册模块
            sql = "INSERT INTO user (email, password, name, faceFeature) VALUES (%s, %s, %s, %s)"
            val = (data['email'], data['password'], data['name'], data['faceFeature'])
            try:
                mycursor.execute(sql, val)
                mydb.commit()    # 数据表内容有更新，必须使用到该语句
            except mysql.connector.errors.IntegrityError:
                #主键重复
                msg = "rep"
            except:
                msg = "error"
                print("注册时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            else:
                msg = "success"
            finally:
                return msg
        elif request.form['method'] == 'login':
            #登录模块
            sql = "SELECT * FROM user WHERE email = %s"
            val = (data['email'], )
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                email = myresult[0][0]
                password = myresult[0][1]
                if(password == data['password']):
                    #验证成功
                    #构造返回json
                    returnData = {
                        "state":"success",
                        "email":email,
                        "name":myresult[0][2]
                    }
                    msg = json.dumps(returnData)
                else:
                    #验证失败
                    returnData = {
                        "state":"fail",
                        "email":"",
                        "name":""
                    }
                    msg = json.dumps(returnData)
            except IndexError:
                #无此账号
                returnData = {
                    "state":"fail",
                    "email":"",
                    "name":""
                }
                msg = json.dumps(returnData)
            except:
                msg = "error"
                print("登录时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
        elif request.form['method'] == 'changePassword':
            #修改密码模块
            sql = "SELECT * FROM user WHERE email = %s"
            val = (data['email'], )
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                originalPassword = myresult[0][1]
                if(data['originalPassword']==originalPassword):
                    #验证成功
                    sql = "UPDATE user SET password = %s WHERE email = %s"
                    val = (data['newPassword'], data['email'])
                    mycursor.execute(sql, val)
                    mydb.commit()
                    msg = "success"
                else:
                    #旧密码错误
                    msg = "fail"
            except:
                msg = "error"
                print("修改密码时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
        elif request.form['method'] == 'getProfile':
            #获取用户信息模块
            sql = "SELECT * FROM user WHERE email = %s"
            val = (data['email'], )
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                result = {
                    "email":myresult[0][0],
                    "password":myresult[0][1],
                    "name":myresult[0][2],
                    "faceFeature":myresult[0][3],
                }
                msg = json.dumps(result)
            except:
                msg = "error"
                print("获取用户信息时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
        elif request.form['method'] == 'addNewEvent':
            #新建考勤事件模块
            if(data['cycle'] == "一次性"):
                sql = "INSERT INTO event (name, creatorEmail, cycle, year, month, day, startHour, startMinute, endHour, endMinute, latitude, longitude, locationRange) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
                val = (data['name'], data['creatorEmail'], data['cycle'], data['year'], data['month'], data['day'], data['startHour'], data['startMinute'], data['endHour'], data['endMinute'], data['latitude'], data['longitude'], data['locationRange'])
            elif(data['cycle'] == "每天"):
                sql = "INSERT INTO event (name, creatorEmail, cycle, startHour, startMinute, endHour, endMinute, latitude, longitude, locationRange) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
                val = (data['name'], data['creatorEmail'], data['cycle'], data['startHour'], data['startMinute'], data['endHour'], data['endMinute'], data['latitude'], data['longitude'], data['locationRange'])
            elif(data['cycle'] == "每星期"):
                sql = "INSERT INTO event (name, creatorEmail, cycle, weekday1, weekday2, weekday3, weekday4, weekday5, weekday6, weekday7, startHour, startMinute, endHour, endMinute, latitude, longitude, locationRange) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
                val = (data['name'], data['creatorEmail'], data['cycle'], data['weekday1'], data['weekday2'], data['weekday3'], data['weekday4'], data['weekday5'], data['weekday6'], data['weekday7'], data['startHour'], data['startMinute'], data['endHour'], data['endMinute'], data['latitude'], data['longitude'], data['locationRange'])
            try:
                mycursor.execute(sql, val)
                returnId = mycursor.lastrowid
                mydb.commit()    # 数据表内容有更新，必须使用到该语句
            except:
                msg = "error"
                print("创建新考勤事件时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            else:
                msg = str(returnId)
            finally:
                return msg
        elif request.form['method'] == 'editEvent':
            #修改考勤事件模块
            sql = "UPDATE event SET name = %s, creatorEmail = %s, cycle = %s, year = %s, month = %s, day = %s, weekday1 = %s, weekday2 = %s, weekday3 = %s, weekday4 = %s, weekday5 = %s, weekday6 = %s, weekday7 = %s, startHour = %s, startMinute = %s, endHour = %s, endMinute = %s, latitude = %s, longitude = %s, locationRange = %s WHERE id = %s"
            if(data['cycle'] == "一次性"):
                val = (data['name'], data['creatorEmail'], data['cycle'], data['year'], data['month'], data['day'], None, None, None, None, None, None, None, data['startHour'], data['startMinute'], data['endHour'], data['endMinute'], data['latitude'], data['longitude'], data['locationRange'], data['id'])
            elif(data['cycle'] == "每天"):
                val = (data['name'], data['creatorEmail'], data['cycle'], None, None, None, None, None, None, None, None, None, None, data['startHour'], data['startMinute'], data['endHour'], data['endMinute'], data['latitude'], data['longitude'], data['locationRange'], data['id'])
            elif(data['cycle'] == "每星期"):
                val = (data['name'], data['creatorEmail'], data['cycle'], None, None, None, data['weekday1'], data['weekday2'], data['weekday3'], data['weekday4'], data['weekday5'], data['weekday6'], data['weekday7'], data['startHour'], data['startMinute'], data['endHour'], data['endMinute'], data['latitude'], data['longitude'], data['locationRange'], data['id'])
            try:
                mycursor.execute(sql, val)
                #returnId = mycursor.lastrowid
                mydb.commit()    # 数据表内容有更新，必须使用到该语句
            except:
                msg = "error"
                print("修改考勤事件时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            else:
                msg = "success"
            finally:
                return msg
        elif request.form['method'] == 'getEventDetail':
            #获取考勤信息模块
            sql = "SELECT * FROM event WHERE id = %s"
            val = (data['id'], )
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                #构造返回json
                returnData = {
                    "state":"success",
                    "id":myresult[0][0],
                    "name":myresult[0][1],
                    "creatorName":myresult[0][2],
                    "cycle":myresult[0][3],
                    "year":myresult[0][4],
                    "month":myresult[0][5],
                    "day":myresult[0][6],
                    "weekday1":myresult[0][7],
                    "weekday2":myresult[0][8],
                    "weekday3":myresult[0][9],
                    "weekday4":myresult[0][10],
                    "weekday5":myresult[0][11],
                    "weekday6":myresult[0][12],
                    "weekday7":myresult[0][13],
                    "startHour":myresult[0][14],
                    "startMinute":myresult[0][15],
                    "endHour":myresult[0][16],
                    "endMinute":myresult[0][17],
                    "latitude":myresult[0][18],
                    "longitude":myresult[0][19],
                    "locationRange":myresult[0][20],
                }
                sql = "SELECT * FROM user WHERE email = %s"
                val = (myresult[0][2], )
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                returnData["creatorName"] = myresult[0][2]
                msg = json.dumps(returnData)
            except IndexError:
                #无此事件
                returnData = {
                    "state":"none",
                    "id":"",
                    "name":"",
                    "creatorName":"",
                    "cycle":"",
                    "year":"",
                    "month":"",
                    "day":"",
                    "weekday1":"",
                    "weekday2":"",
                    "weekday3":"",
                    "weekday4":"",
                    "weekday5":"",
                    "weekday6":"",
                    "weekday7":"",
                    "startHour":"",
                    "startMinute":"",
                    "endHour":"",
                    "endMinute":"",
                    "latitude":"",
                    "longitude":"",
                    "locationRange":"",
                }
                msg = json.dumps(returnData)
            except:
                msg = "error"
                print("获取考勤事件信息时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
        elif request.form['method'] == 'getEventList':
            #获取考勤事件列表模块
            #SQL：一次获取五条，需要一个offset
            sql = "SELECT * FROM event WHERE creatorEmail = %s ORDER BY id DESC LIMIT 5 OFFSET %s"
            val = (data['email'], int(data['offset']))
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                if(len(myresult)<5):
                    #数据查完了
                    resultSet = {
                        "state":"noMore",
                        "result":[]
                    }
                else:
                    resultSet = {
                        "state":"succsee",
                        "result":[]
                    }
                for row in myresult:
                    returnData = {
                        "id":row[0],
                        "name":row[1],
                        "creatorName":row[2],
                        "cycle":row[3],
                        "year":row[4],
                        "month":row[5],
                        "day":row[6],
                        "weekday1":row[7],
                        "weekday2":row[8],
                        "weekday3":row[9],
                        "weekday4":row[10],
                        "weekday5":row[11],
                        "weekday6":row[12],
                        "weekday7":row[13],
                        "startHour":row[14],
                        "startMinute":row[15],
                        "endHour":row[16],
                        "endMinute":row[17],
                        "latitude":row[18],
                        "longitude":row[19],
                        "locationRange":row[20],
                    }
                    resultSet["result"].append(returnData)
                msg = json.dumps(resultSet)
            except:
                msg = "error"
                print("获取考勤事件列表时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
        elif request.form['method'] == 'participant':
            #考勤模块
            nowTime = time.time()
            nowTimeStruct = time.localtime(nowTime)
            sql = "INSERT INTO participance (eventId, participantEmail, year, month, day, hour, minute) VALUES (%s, %s, %s, %s, %s, %s, %s)"
            val = (data['eventId'], data['participantEmail'], str(nowTimeStruct.tm_year), str(nowTimeStruct.tm_mon), str(nowTimeStruct.tm_mday), str(nowTimeStruct.tm_hour), str(nowTimeStruct.tm_min))
            try:
                mycursor.execute(sql, val)
                mydb.commit()    # 数据表内容有更新，必须使用到该语句
            except:
                msg = "error"
                print("考勤时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            else:
                msg = "success"
            finally:
                return msg
        elif request.form['method'] == 'checkCanParticipant':
            #检查是否能考勤模块
            #检查时间段
            #检查是否已经考勤过
            # 获取考勤事件信息
            sql = "SELECT * FROM event WHERE id = %s"
            val = (data['eventId'], )
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                nowTime = time.time()
                nowTimeStruct = time.localtime(nowTime)
                inTime = False
                notYet = False
                if(myresult[0][3] == "一次性"):
                    startTime = time.mktime((int(myresult[0][4]), int(myresult[0][5]), int(myresult[0][6]), int(myresult[0][14]), int(myresult[0][15]),0,0,0,0))
                    endTime = time.mktime((int(myresult[0][4]), int(myresult[0][5]), int(myresult[0][6]), int(myresult[0][16]), int(myresult[0][17]),0,0,0,0))
                    if(endTime<startTime):
                        endTime = time.mktime((int(myresult[0][4]), int(myresult[0][5]), int(myresult[0][6])+1, int(myresult[0][16]), int(myresult[0][17]),0,0,0,0))
                    if(nowTime>=startTime and nowTime<=endTime):
                        inTime = True
                elif(myresult[0][3] == "每天"):
                    startTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2], int(myresult[0][14]), int(myresult[0][15]),0,0,0,0))
                    endTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2], int(myresult[0][16]), int(myresult[0][17]),0,0,0,0))
                    if(nowTime>=startTime or nowTime<=endTime):
                        inTime = True
                elif(myresult[0][3] == "每星期"):
                    startTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2], int(myresult[0][14]), int(myresult[0][15]),0,0,0,0))
                    endTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2], int(myresult[0][16]), int(myresult[0][17]),0,0,0,0))
                    if(nowTime>=startTime):
                        #在当天的时间段
                        if(myresult[0][7+nowTimeStruct.tm_wday] == "true"):
                            inTime = True
                    if(nowTime<=endTime):
                        #在次日的时间段
                        if(nowTimeStruct.tm_wday == 0):
                            if(myresult[0][13] == "true"):
                                inTime = True
                        else:
                            if(myresult[0][7+nowTimeStruct.tm_wday-1] == "true"):
                                inTime = True
                if(inTime == True):
                    startTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2], int(myresult[0][14]), int(myresult[0][15]),0,0,0,0))
                    endTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2], int(myresult[0][16]), int(myresult[0][17]),0,0,0,0))
                    if(endTime<startTime):
                        if(nowTime<endTime):
                            startTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2]-1, int(myresult[0][14]), int(myresult[0][15]),0,0,0,0))
                        else:
                            endTime = time.mktime((nowTimeStruct[0], nowTimeStruct[1], nowTimeStruct[2]+1, int(myresult[0][16]), int(myresult[0][17]),0,0,0,0))
                    #判断是否已经考勤过
                    sql = "SELECT * FROM participance WHERE participantEmail = %s AND eventId = %s ORDER BY id DESC LIMIT 1"
                    val = (data['participantEmail'], data['eventId'])
                    mycursor.execute(sql, val)
                    myresult = mycursor.fetchall()
                    if(len(myresult)==0):
                        #没考勤过
                        notYet = True
                    else:
                        #判断该考勤记录是否在当前时间段内
                        participanceTime = time.mktime((int(myresult[0][3]), int(myresult[0][4]), int(myresult[0][5]), int(myresult[0][6]), int(myresult[0][7]),0,0,0,0))
                        if(participanceTime<startTime or participanceTime>endTime):
                            notYet = True
                msg = {
                    "inTime":inTime,
                    "notYet":notYet
                }
            except:
                msg = "error"
                print("检查是否能考勤时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
        elif request.form['method'] == 'getHistoryList':
            #获取考勤历史列表模块
            #SQL：一次获取五条，需要一个offset
            sql = "SELECT * FROM participance WHERE participantEmail = %s ORDER BY id DESC LIMIT 5 OFFSET %s"
            val = (data['email'], int(data['offset']))
            try:
                mycursor.execute(sql, val)
                myresult = mycursor.fetchall()
                if(len(myresult)<5):
                    #数据查完了
                    resultSet = {
                        "state":"noMore",
                        "result":[]
                    }
                else:
                    resultSet = {
                        "state":"succsee",
                        "result":[]
                    }
                for row in myresult:
                    temoCursor = mydb.cursor()
                    tempsql = "SELECT name FROM event WHERE id = %s"
                    tempval = (row[1], )
                    temoCursor.execute(tempsql, tempval)
                    tempResult = temoCursor.fetchall()
                    returnData = {
                        "id":row[0],
                        "eventName":tempResult[0][0],
                        "participantEmail":row[2],
                        "year":row[3],
                        "month":row[4],
                        "day":row[5],
                        "hour":row[6],
                        "minute":row[7]
                    }
                    resultSet["result"].append(returnData)
                msg = json.dumps(resultSet)
            except:
                msg = "error"
                print("获取考勤历史列表时遇到错误:", sys.exc_info()[0], sys.exc_info()[1])
            finally:
                return msg
    elif request.method == 'GET':
        return "不适用GET方法"

if __name__ == '__main__':
    app.run("0.0.0.0")