package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.annotation.*;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**实体类信息定义*/
public class DefaultTableDefiner implements TableDefiner{
    private static Logger logger = LoggerFactory.getLogger(DefaultTableDefiner.class);
    /**用于返回QuickDAO对象*/
    private QuickDAO quickDAO;
    /**用于返回QuickDAO对象*/
    private QuickDAOConfig quickDAOConfig;
    /**当前实体类*/
    private Entity entity;

    public DefaultTableDefiner(QuickDAO quickDAO, QuickDAOConfig quickDAOConfig) {
        this.quickDAO = quickDAO;
        this.quickDAOConfig = quickDAOConfig;
    }

    public TableDefiner define(Class clazz) {
        this.entity = quickDAOConfig.entityMap.get(clazz.getName());
        return this;
    }

    @Override
    public TableDefiner tableName(String tableName) {
        entity.tableName = tableName;
        return this;
    }

    @Override
    public TableDefiner comment(String comment) {
        entity.comment = comment;
        return this;
    }

    @Override
    public TablePropertyDefiner property(String fieldName) {
        for(Property property:entity.properties){
            if(property.name.equals(fieldName)){
                return new DefaultTablePropertyDefiner(property,this);
            }
        }
        return null;
    }

    @Override
    public QuickDAO done() {
        return this.quickDAO;
    }

    /**
     * 获取实体类信息
     */
    public synchronized void getEntityMap() throws Exception {
        Set<String> keySet = quickDAOConfig.packageNameMap.keySet();
        for (String packageName : keySet) {
            List<Class> classList = scanEntity(packageName);
            for (Class c : classList) {
                Entity entity = new Entity();
                if(c.getDeclaredAnnotation(TableName.class)!=null){
                    entity.tableName = ((TableName) c.getDeclaredAnnotation(TableName.class)).value();
                }else if ((packageName.length() + c.getSimpleName().length() + 1) == c.getName().length()) {
                    entity.tableName = quickDAOConfig.packageNameMap.get(packageName)+StringUtil.Camel2Underline(c.getSimpleName());
                } else {
                    String prefix = c.getName().substring(packageName.length() + 1, c.getName().lastIndexOf(".")).replace(".", "_");
                    entity.tableName = quickDAOConfig.packageNameMap.get(packageName)+prefix + "@" + StringUtil.Camel2Underline(c.getSimpleName());
                }
                entity.clazz = c;
                quickDAOConfig.entityMap.put(c.getName(), entity);
            }
            for (Class c : classList) {
                Entity entity = quickDAOConfig.entityMap.get(c.getName());
                entity.className = c.getSimpleName();
                if (c.getDeclaredAnnotation(Comment.class) != null) {
                    Comment comment = (Comment) c.getDeclaredAnnotation(Comment.class);
                    entity.comment = comment.value();
                }
                //属性列表
                List<Property> propertyList = new ArrayList<>();
                //实体包类列表
                List<Field> compositFieldList = new ArrayList<>();
                //添加字段信息
                {
                    Field[] fields = c.getDeclaredFields();
                    Field.setAccessible(fields, true);
                    for(Field field:fields){
                        if (field.getDeclaredAnnotation(Ignore.class) != null) {
                            logger.debug("[跳过实体属性]{},该属性被Ignore注解修饰!", field.getName());
                            continue;
                        }
                        if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers())){
                            logger.debug("[跳过常量或静态变量]{},该属性被static或者final修饰!", field.getName());
                            continue;
                        }
                        //跳过实体包类
                        if (isCompositProperty(field.getType())) {
                            compositFieldList.add(field);
                            continue;
                        }
                        //跳过List类型和数组类型
                        if(field.getType().isArray()||(!field.getType().isPrimitive()&&isCollection(field.getType()))){
                            continue;
                        }
                        Property property = new Property();
                        if (null!=field.getAnnotation(ColumnName.class)) {
                            property.column = field.getAnnotation(ColumnName.class).value();
                        }else{
                            property.column = StringUtil.Camel2Underline(field.getName());
                        }
                        if(null!=field.getAnnotation(ColumnType.class)){
                            property.columnType = field.getAnnotation(ColumnType.class).value();
                        }
                        property.name = field.getName();
                        property.simpleTypeName = field.getType().getSimpleName().toLowerCase();
                        Constraint constraint = field.getDeclaredAnnotation(Constraint.class);
                        if(null!=constraint){
                            property.notNull = constraint.notNull();
                            property.unique = constraint.unique();
                            property.check = constraint.check();
                            property.defaultValue = constraint.defaultValue();
                        }
                        property.id = field.getDeclaredAnnotation(Id.class) != null || "id".equals(property.column);
                        property.index = field.getDeclaredAnnotation(Index.class) != null;
                        if(null!=field.getDeclaredAnnotation(Comment.class)){
                            property.comment = field.getDeclaredAnnotation(Comment.class).value();
                        }
                        property.foreignKey = field.getDeclaredAnnotation(ForeignKey.class);
                        property.entity = entity;
                        if(property.id){
                            entity.id = property;
                        }
                        propertyList.add(property);
                    }
                }
                entity.properties = propertyList.toArray(new Property[0]);
                if (compositFieldList.size() > 0) {
                    entity.compositFields = compositFieldList.toArray(new Field[0]);
                }
            }
        }
        logger.debug("[获取实体信息]实体类个数:{}", quickDAOConfig.entityMap.size());
    }

    /**处理实体类相关约束*/
    public synchronized void handleEntityMap(){
        for(Entity entity: quickDAOConfig.entityMap.values()){
            List<Property> indexPropertyList = new ArrayList<>();
            List<Property> uniquePropertyList = new ArrayList<>();
            List<Property> checkPropertyList = new ArrayList<>();
            List<Property> foreignKeyPropertyList = new ArrayList<>();
            for(Property property : entity.properties){
                if(property.id){
                    property.notNull = true;
                }
                if(property.unique){
                    property.notNull = true;
                    property.index = true;
                }
                if(property.index){
                    indexPropertyList.add(property);
                }
                if(property.unique){
                    uniquePropertyList.add(property);
                }
                if(null!=property.check&&!property.check.isEmpty()){
                    checkPropertyList.add(property);
                }
                if(null!=property.foreignKey){
                    foreignKeyPropertyList.add(property);
                }
            }
            entity.indexProperties = indexPropertyList.toArray(new Property[0]);
            entity.uniqueKeyProperties = uniquePropertyList.toArray(new Property[0]);
            entity.checkProperties = checkPropertyList.toArray(new Property[0]);
            entity.foreignKeyProperties = foreignKeyPropertyList.toArray(new Property[0]);
        }
    };

    /**
     * 扫描实体包
     */
    private List<Class> scanEntity(String packageName) throws ClassNotFoundException, IOException {
        String packageNamePath = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(packageNamePath);
        if (url == null) {
            throw new IllegalArgumentException("无法识别的包路径:" + packageNamePath);
        }
        List<Class> classList = new ArrayList<>();
        switch (url.getProtocol()) {
            case "file": {
                File file = new File(url.getFile());
                //TODO 对于有空格或者中文路径会无法识别
                logger.info("[类文件路径]{}", file.getAbsolutePath());
                if (!file.isDirectory()) {
                    throw new IllegalArgumentException("包名不是合法的文件夹!" + url.getFile());
                }
                Stack<File> stack = new Stack<>();
                stack.push(file);

                String indexOfString = packageName.replace(".", "/");
                while (!stack.isEmpty()) {
                    file = stack.pop();
                    for (File f : file.listFiles()) {
                        if (f.isDirectory()) {
                            stack.push(f);
                        } else if (f.isFile() && f.getName().endsWith(".class")) {
                            String path = f.getAbsolutePath().replace("\\", "/");
                            int startIndex = path.indexOf(indexOfString);
                            String className = path.substring(startIndex, path.length() - 6).replace("/", ".");
                            classList.add(Class.forName(className));
                        }
                    }
                }
            }
            break;
            case "jar": {
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                if (null != jarURLConnection) {
                    JarFile jarFile = jarURLConnection.getJarFile();
                    if (null != jarFile) {
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()) {
                            JarEntry jarEntry = jarEntries.nextElement();
                            String jarEntryName = jarEntry.getName();
                            if (jarEntryName.contains(packageNamePath) && jarEntryName.endsWith(".class")) { //是否是类,是类进行加载
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                classList.add(Class.forName(className));
                            }
                        }
                    }
                }
            }
            break;
        }
        if (classList.size() == 0) {
            logger.warn("[扫描实体类信息为空]前缀:{},包名:{}", quickDAOConfig.packageNameMap.get(packageName), packageName);
            return classList;
        }
        Stream<Class> stream = classList.stream().filter((_class) -> {
            if(_class.isEnum()){
                return false;
            }
            if (_class.getAnnotation(Ignore.class) != null) {
                logger.debug("[忽略实体类]类名:{},原因:@Ignore注解.", _class.getName());
                return false;
            }
            boolean result = true;
            //根据类过滤
            if (quickDAOConfig.ignoreClassList != null) {
                if (quickDAOConfig.ignoreClassList.contains(_class)) {
                    logger.debug("[忽略实体类]类名:{},原因:忽略该类.", _class.getName());
                    result = false;
                }
            }
            //根据包名过滤
            if (quickDAOConfig.ignorePackageNameList != null) {
                for (String ignorePackageName : quickDAOConfig.ignorePackageNameList) {
                    if (_class.getName().contains(ignorePackageName)) {
                        logger.warn("[忽略实体类]类名:{},原因:该类所在包被忽略.所在包:{}", _class.getName(),ignorePackageName);
                        result = false;
                    }
                }
            }
            if(null!=quickDAOConfig.predicate){
                logger.debug("[忽略实体类]类名:{},原因:过滤接口返回false!", _class.getName());
                result = quickDAOConfig.predicate.test(_class);
            }
            return result;
        });
        classList = stream.collect(Collectors.toList());
        return classList;
    }

    /**
     * 判断是否是实体包类
     **/
    private boolean isCompositProperty(Class clazz) {
        Set<String> packageNameSet = quickDAOConfig.packageNameMap.keySet();
        for (String packageName : packageNameSet) {
            if (clazz.getName().contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**是否集合*/
    private boolean isCollection(Class _class){
        Stack<Class[]> stack = new Stack<>();
        stack.push(_class.getInterfaces());
        while(!stack.isEmpty()){
            Class[] classes = stack.pop();
            for(Class clazz:classes){
                if(clazz.getName().equals(Collection.class.getName())){
                    return true;
                }
                Class[] subClasses = clazz.getInterfaces();
                if(null!=subClasses&&subClasses.length>0){
                    stack.push(subClasses);
                }
            }
        }
        return false;
    }
}
