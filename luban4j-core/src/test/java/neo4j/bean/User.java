package neo4j.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.Listen;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Date;
import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2023
 */

@Listen
@NodeEntity("ZZZUSER")
public class User  extends AbstractUser{

   /* @Id
    @GeneratedValue(strategy = UuidStrategy.class)*/
    String id;
    String name;
    UserAddress address;
    List<Label> labels;
    int count;
    Date time;
    char c;

    Float theFloat;
    Float [] theFloats;
    Double theDouble;
    List<Double> theDoubles;
    Byte theByte;
    Boolean theBoolean;
    Short theShort;

    @Labels
    List<String> neo4jLabels;



    public User(String id) {
        this.id = id;
    }

    public User() {
    }

 public String getId() {
  return id;
 }

 public void setId(String id) {
  this.id = id;
 }

 public String getName() {
  return name;
 }

 public void setName(String name) {
  this.name = name;
 }

 public UserAddress getAddress() {
  return address;
 }

 public void setAddress(UserAddress address) {
  this.address = address;
 }

 public List<Label> getLabels() {
  return labels;
 }

 public void setLabels(List<Label> labels) {
  this.labels = labels;
 }

 public int getCount() {
  return count;
 }

 public void setCount(int count) {
  this.count = count;
 }

 public Date getTime() {
  return time;
 }

 public void setTime(Date time) {
  this.time = time;
 }

 public char getC() {
  return c;
 }

 public void setC(char c) {
  this.c = c;
 }

 public Float getTheFloat() {
  return theFloat;
 }

 public void setTheFloat(Float theFloat) {
  this.theFloat = theFloat;
 }

 public Float[] getTheFloats() {
  return theFloats;
 }

 public void setTheFloats(Float[] theFloats) {
  this.theFloats = theFloats;
 }

 public Double getTheDouble() {
  return theDouble;
 }

 public void setTheDouble(Double theDouble) {
  this.theDouble = theDouble;
 }

 public List<Double> getTheDoubles() {
  return theDoubles;
 }

 public void setTheDoubles(List<Double> theDoubles) {
  this.theDoubles = theDoubles;
 }

 public Byte getTheByte() {
  return theByte;
 }

 public void setTheByte(Byte theByte) {
  this.theByte = theByte;
 }

 public Boolean getTheBoolean() {
  return theBoolean;
 }

 public void setTheBoolean(Boolean theBoolean) {
  this.theBoolean = theBoolean;
 }

 public Short getTheShort() {
  return theShort;
 }

 public void setTheShort(Short theShort) {
  this.theShort = theShort;
 }

 public List<String> getNeo4jLabels() {
  return neo4jLabels;
 }

 public void setNeo4jLabels(List<String> neo4jLabels) {
  this.neo4jLabels = neo4jLabels;
 }
}

