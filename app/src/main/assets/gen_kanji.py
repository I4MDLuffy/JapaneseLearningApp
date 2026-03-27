#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import re, csv, sys

ASSETS = r"C:\Users\ARSRa\AndroidStudioProjects\PersonalProject\app\src\main\assets"
SRC = ASSETS + r"\KanjiReferences.txt"
OUT = ASSETS + r"\kanji.csv"

HEADER = ["id","kanji","meaning","hiragana","on_yomi","kun_yomi","vocab_references",
          "jlpt_level","theme","unicode","stroke_order_image","radical_references",
          "stroke_count","grade_level","component_structure","lesson_number"]

def clean_dash(s):
    # Replace non-breaking hyphen U+2011 and similar with empty (remove trailing dashes from readings)
    s = s.replace('\u2011', '-').replace('\u2010', '-')
    # strip trailing dashes
    s = s.rstrip('-')
    return s.strip()

def clean_reading(s):
    s = s.strip()
    if s == '(N/A)':
        return ''
    s = clean_dash(s)
    return s

def clean_radicals(s):
    """Keep only r+digits entries, zero-pad to 3 digits."""
    s = s.strip()
    parts = [p.strip() for p in s.split(',')]
    result = []
    for p in parts:
        m = re.match(r'^r(\d+)$', p)
        if m:
            num = int(m.group(1))
            result.append('r{:03d}'.format(num))
    return ','.join(result)

def parse_id_num(id_str):
    m = re.match(r'k(\d+)', id_str)
    if m:
        return int(m.group(1))
    return 0

def n3_lesson(id_num):
    """Reassign lesson numbers for N3 kanji k0247-k0616."""
    # k0247-k0406: lessons 1-8 (20 each), already correct in data for k0247-k0406
    # But source has all N3 after k0406 as lesson 9, so we reassign from k0407:
    if id_num <= 0:
        return '9'
    if id_num <= 0:
        return '1'
    # k0247-k0266: lesson 1 (20 kanji)
    if id_num <= 266:
        return '1'
    # We compute based on position within N3 block
    # N3 starts at k0247
    pos = id_num - 0x0247 + 1  # 1-based position, but let's use decimal
    pos = id_num - 247 + 1  # 1-indexed position in N3 block
    lesson = ((pos - 1) // 20) + 1
    if lesson > 19:
        lesson = 19
    return str(lesson)

def wrap_meaning(m):
    if ',' in m:
        return '"' + m.replace('"', '""') + '"'
    return m

rows = []

with open(SRC, encoding='utf-8') as f:
    for line in f:
        line = line.rstrip('\n').rstrip('\r')
        # Tab-separated data lines start with k followed by digits
        parts = re.split(r'  +', line.rstrip())
        if len(parts) < 9:
            continue
        id_str = parts[0].strip()
        if not re.match(r'^k\d+$', id_str):
            continue

        # Pad with empty strings to avoid index errors
        while len(parts) < 15:
            parts.append('')
        kanji   = parts[1].strip()
        meaning = parts[2].strip()
        hiragana= clean_reading(parts[3])
        on_yomi = clean_reading(parts[4])
        kun_yomi= clean_reading(parts[5])
        jlpt    = parts[6].strip()
        theme_raw = parts[7].strip()

        # Detect theme/unicode merge in col7: "theme/U+XXXX"
        merged = ('/' in theme_raw and 'U+' in theme_raw)
        if merged:
            # col8=radical, col9=strokes, col10=grade, col11=comp, col12=lesson
            slash = theme_raw.index('/')
            theme = theme_raw[:slash]
            unicode_val = theme_raw[slash+1:]
            radical_raw  = parts[8].strip()
            stroke_count = parts[9].strip() if parts[9].strip() else '0'
            grade_level  = parts[10].strip() if parts[10].strip() else '0'
            comp_struct  = parts[11].strip() if parts[11].strip() else 'single'
            lesson_raw   = parts[12].strip() if parts[12].strip() else '1'
        else:
            # col8=unicode, col9=radical, col10=strokes, col11=grade, col12=comp, col13=lesson
            unicode_val = parts[8].strip()
            radical_raw  = parts[9].strip()
            stroke_count = parts[10].strip() if parts[10].strip() else '0'
            grade_level  = parts[11].strip() if parts[11].strip() else '0'
            comp_struct  = parts[12].strip() if parts[12].strip() else 'single'
            lesson_raw   = parts[13].strip() if parts[13].strip() else '1'
            theme = theme_raw

        # Normalize jlpt
        jlpt = jlpt.strip()
        if jlpt not in ('N5','N4','N3','N2','N1'):
            jlpt = 'N3'  # fallback

        # Fix unicode if it ended up in radical column due to merge
        if 'U+' in radical_raw and unicode_val == '':
            unicode_val = radical_raw
            radical_raw = ''

        radicals = clean_radicals(radical_raw)

        # Lesson number reassignment for N3
        id_num = parse_id_num(id_str)
        if jlpt == 'N3' and id_num >= 247:
            lesson = n3_lesson(id_num)
        else:
            lesson = lesson_raw.strip()

        row = [
            id_str, kanji, meaning, hiragana, on_yomi, kun_yomi,
            '',  # vocab_references
            jlpt, theme, unicode_val,
            '',  # stroke_order_image
            radicals, stroke_count, grade_level, comp_struct, lesson
        ]
        rows.append(row)

# Now add N2 kanji starting at k0617
n2_data = [
    # id_suffix, kanji, meaning, hiragana, on_yomi, kun_yomi, theme, unicode, radicals, strokes, grade, comp, lesson
    (617,'区','ward; district','く','ク','','place','U+533A','r022',4,3,'enclosure',1),
    (618,'県','prefecture','けん','ケン','','society','U+770C','r162',9,3,'top-bottom',1),
    (619,'村','village','むら','ソン','むら','place','U+6751','r075,r036',7,1,'left-right',1),
    (620,'低','low','ひくい','テイ','ひくい','description','U+4F4E','r012,r075',7,3,'left-right',1),
    (621,'門','gate','もん','モン','もん','object','U+9580','r263',8,2,'enclosure',1),
    (622,'森','forest','もり','シン','もり','nature','U+68EE','r113',12,1,'left-right',1),
    (623,'林','woods','はやし','リン','はやし','nature','U+6797','r113',8,1,'left-right',1),
    (624,'短','short','みじかい','タン','みじかい','description','U+77ED','r169,r046',12,3,'left-right',1),
    (625,'軽','light (weight)','かるい','ケイ','かるい','description','U+8EFD','r245',12,3,'left-right',1),
    (626,'池','pond','いけ','チ','いけ','nature','U+6C60','r090,r004',6,2,'left-right',1),
    (627,'弱','weak','よわい','ジャク','よわい','description','U+5F31','r057',10,2,'left-right',1),
    (628,'菜','vegetable','な','サイ','な','food','U+83DC','r083',11,4,'top-bottom',1),
    (629,'協','cooperate','きょう','キョウ','','society','U+5354','r026',8,4,'left-right',1),
    (630,'改','reform','あらためる','カイ','あらためる','action','U+6539','r101,r065',7,4,'left-right',1),
    (631,'府','city office','ふ','フ','','society','U+5E9C','r072',8,4,'enclosure',1),
    (632,'委','entrust','い','イ','','action','U+59D4','r048,r004',8,3,'top-bottom',1),
    (633,'軍','army','ぐん','グン','','society','U+8ECD','r245',9,4,'enclosure',1),
    (634,'各','each','おのおの','カク','おのおの','abstract','U+5404','r029,r037',6,4,'top-bottom',1),
    (635,'島','island','しま','トウ','しま','nature','U+5CF6','r059',10,3,'single',1),
    (636,'副','vice; secondary','ふく','フク','','abstract','U+526F','r024',11,4,'left-right',1),
    (637,'算','calculate','さんする','サン','','number','U+7B97','r118,r052',14,2,'top-bottom',2),
    (638,'線','line','せん','セン','','abstract','U+7DDA','r189',15,2,'left-right',2),
    (639,'農','farming','のう','ノウ','','society','U+8FB2','r084',13,3,'top-bottom',2),
    (640,'州','state; province','しゅう','シュウ','','place','U+5DDE','r064',6,3,'single',2),
    (641,'象','elephant; symbol','ぞう','ショウ','','nature','U+8C61','r130',12,5,'single',2),
    (642,'賞','prize','しょう','ショウ','','abstract','U+8CDE','r236,r047',15,4,'top-bottom',2),
    (643,'辺','area; vicinity','へん','ヘン','あたり','position','U+8FBA','r249',5,4,'left-right',2),
    (644,'課','section; lesson','か','カ','','society','U+8AB2','r228,r151',15,4,'left-right',2),
    (645,'極','extreme','きわめる','キョク','きわめる','abstract','U+6975','r113,r126',12,4,'left-right',2),
    (646,'量','quantity','りょう','リョウ','はかる','abstract','U+91CF','r150',12,4,'single',2),
    (647,'型','model; type','かた','ケイ','かた','abstract','U+578B','r040,r094',9,4,'top-bottom',2),
    (648,'谷','valley','たに','コク','たに','nature','U+8C37','r076',7,2,'single',2),
    (649,'史','history','し','シ','','abstract','U+53F2','r037',5,4,'single',2),
    (650,'階','floor; level','かい','カイ','','place','U+968E','r086,r040',12,3,'left-right',2),
    (651,'管','pipe; manage','くだ','カン','くだ','object','U+7BA1','r118',14,4,'top-bottom',2),
    (652,'兵','soldier','へい','ヘイ,ヒョウ','','society','U+5175','r016',7,4,'top-bottom',2),
    (653,'細','thin; detailed','ほそい','サイ','ほそい','description','U+7D30','r189',11,2,'left-right',2),
    (654,'丸','round; circle','まる','ガン','まる','description','U+4E38','r004',3,2,'single',2),
    (655,'録','record','ろく','ロク','','action','U+9332','r260',16,4,'left-right',2),
    (656,'省','ministry; save','はぶく','ショウ,セイ','はぶく','society','U+7701','r162',9,4,'top-bottom',2),
    (657,'橋','bridge','はし','キョウ','はし','object','U+6A4B','r113,r126',16,3,'left-right',3),
    (658,'岸','shore','きし','ガン','きし','nature','U+5CB8','r046,r060',8,3,'top-bottom',3),
    (659,'周','circumference','まわり','シュウ','まわり','abstract','U+5468','r037',8,4,'enclosure',3),
    (660,'材','material','ざい','ザイ','','object','U+6750','r113,r046',7,4,'left-right',3),
    (661,'戸','door','と','コ','と','object','U+6238','r068',4,2,'single',3),
    (662,'央','center','おう','オウ','','position','U+592E','r037,r047',5,3,'single',3),
    (663,'竹','bamboo','たけ','チク','たけ','nature','U+7AF9','r118',6,1,'single',3),
    (664,'競','compete','きそう','キョウ,ケイ','きそう','action','U+7AF6','r176',20,4,'left-right',3),
    (665,'根','root','ね','コン','ね','nature','U+6839','r113,r067',10,3,'left-right',3),
    (666,'歴','history','れき','レキ','','abstract','U+6B74','r072,r115',14,4,'left-right',3),
    (667,'航','navigate','こう','コウ','','action','U+822A','r137',10,4,'left-right',3),
    (668,'鉄','iron','てつ','テツ','','object','U+9244','r260,r046',13,3,'left-right',3),
    (669,'児','child','こ','ジ','','people','U+5150','r014',8,4,'single',3),
    (670,'印','stamp; mark','しるし','イン','しるし','abstract','U+5370','r026,r048',6,4,'left-right',3),
    (671,'油','oil','あぶら','ユ','あぶら','object','U+6CB9','r090,r152',8,3,'left-right',3),
    (672,'輪','wheel; ring','わ','リン','わ','object','U+8F2A','r245',15,4,'left-right',3),
    (673,'植','plant','うえる','ショク','うえる','nature','U+690D','r113,r004',12,3,'left-right',3),
    (674,'清','clear; pure','きよい','セイ,ショウ','きよい','nature','U+6E05','r090,r040',11,4,'left-right',3),
    (675,'倍','double','ばい','バイ','','number','U+500D','r012,r037',10,3,'left-right',3),
    (676,'億','hundred million','おく','オク','','number','U+5104','r012',15,4,'left-right',3),
    (677,'芸','art; craft','げい','ゲイ','','art','U+82B8','r083',7,4,'top-bottom',4),
    (678,'停','stop; halt','とまる','テイ','とまる','action','U+505C','r012,r040',11,4,'left-right',4),
    (679,'陸','land','りく','リク','','nature','U+9678','r086,r040',11,4,'left-right',4),
    (680,'玉','jewel; ball','たま','ギョク','たま','object','U+7389','r146',5,1,'single',4),
    (681,'波','wave','なみ','ハ','なみ','nature','U+6CE2','r090,r123',8,3,'left-right',4),
    (682,'帯','belt; zone','おび','タイ','おび','object','U+5E2F','r068',10,4,'top-bottom',4),
    (683,'羽','feather; wing','はね','ウ','はね','nature','U+7FBD','r196',6,2,'single',4),
    (684,'固','hard; solid','かたい','コ','かたい','description','U+56FA','r039,r037',8,4,'enclosure',4),
    (685,'囲','surround','かこむ','イ','かこむ','action','U+56F2','r039,r037',7,4,'enclosure',4),
    (686,'卒','graduate','そつ','ソツ','','education','U+5352','r039',8,4,'top-bottom',4),
    (687,'順','order; obey','じゅん','ジュン','','abstract','U+9806','r279,r037',12,4,'left-right',4),
    (688,'岩','rock','いわ','ガン','いわ','nature','U+5CA9','r046,r060',8,2,'top-bottom',4),
    (689,'練','practice','ねる','レン','ねる','action','U+7DF4','r189,r046',14,4,'left-right',4),
    (690,'令','order; command','れい','レイ','','abstract','U+4EE4','r011',5,4,'top-bottom',4),
    (691,'角','angle; horn','かど','カク','かど','abstract','U+89D2','r148',7,2,'single',4),
    (692,'貨','goods; cargo','か','カ','','object','U+8CA8','r236',11,4,'top-bottom',4),
    (693,'血','blood','ち','ケツ','ち','body','U+8840','r155',6,3,'single',4),
    (694,'温','warm','あたたかい','オン','あたたかい','nature','U+6E29','r090,r040',12,3,'left-right',4),
    (695,'季','season','き','キ','','time','U+5B63','r050,r046',8,4,'top-bottom',4),
    (696,'星','star','ほし','セイ,ショウ','ほし','nature','U+661F','r107,r174',9,2,'top-bottom',4),
    (697,'庫','warehouse','くら','コ','くら','place','U+5EAB','r072,r245',10,3,'enclosure',5),
    (698,'坂','slope','さか','ハン','さか','nature','U+5742','r040,r029',7,3,'left-right',5),
    (699,'底','bottom','そこ','テイ','そこ','position','U+5E95','r072,r004',8,4,'enclosure',5),
    (700,'寺','temple','てら','ジ','てら','place','U+5BFA','r040,r053',6,2,'top-bottom',5),
    (701,'希','hope; rare','のぞむ','キ','のぞむ','abstract','U+5E0C','r068',7,4,'top-bottom',5),
    (702,'仲','relationship; middle','なか','チュウ','なか','people','U+4EC6','r012,r039',6,4,'left-right',5),
    (703,'栄','flourish','さかえる','エイ','さかえる','abstract','U+6804','r113',9,4,'top-bottom',5),
    (704,'札','tag; bill','ふだ','サツ','ふだ','object','U+672D','r113',5,4,'left-right',5),
    (705,'板','board; plank','いた','ハン,バン','いた','object','U+677F','r113,r029',8,3,'left-right',5),
    (706,'包','wrap','つつむ','ホウ','つつむ','action','U+5305','r021',5,4,'enclosure',5),
    (707,'焼','burn; roast','やく','ショウ','やく','action','U+713C','r128,r040',12,4,'left-right',5),
    (708,'章','chapter; badge','しょう','ショウ','','abstract','U+7AE0','r176',11,3,'top-bottom',5),
    (709,'照','illuminate','てらす','ショウ','てらす','action','U+7167','r107,r126',13,4,'top-bottom',5),
    (710,'秒','second (time)','びょう','ビョウ','','time','U+79D2','r173,r046',9,3,'left-right',5),
    (711,'皮','skin; leather','かわ','ヒ','かわ','body','U+76AE','r030',5,3,'single',5),
    (712,'漁','fishing','りょう','ギョ,リョウ','','action','U+6F01','r090,r203',14,4,'left-right',5),
    (713,'貯','save; store','たくわえる','チョ','たくわえる','action','U+8CBC','r236,r039',12,4,'left-right',5),
    (714,'柱','pillar','はしら','チュウ','はしら','object','U+6932','r113,r004',9,3,'left-right',5),
    (715,'祭','festival','まつり','サイ','まつり','society','U+796D','r142',11,3,'top-bottom',5),
    (716,'筆','brush; writing','ふで','ヒツ','ふで','object','U+7B46','r118,r048',12,3,'top-bottom',5),
]

for d in n2_data:
    (suf, kanji, meaning, hiragana, on_yomi, kun_yomi, theme, unicode_val, radicals_raw, strokes, grade, comp, lesson) = d
    id_str = 'k{:04d}'.format(suf)
    radicals = clean_radicals(radicals_raw)
    row = [id_str, kanji, meaning, hiragana, on_yomi, kun_yomi,
           '', 'N2', theme, unicode_val, '', radicals,
           str(strokes), str(grade), comp, str(lesson)]
    rows.append(row)

# N1 kanji starting at k0717
n1_data = [
    (717,'暑','hot (weather)','あつい','ショ','あつい','nature','U+6691','r107,r040',12,3,'left-right',1),
    (718,'第','ordinal prefix','だい','ダイ','','abstract','U+7B2C','r118,r040',11,3,'top-bottom',1),
    (719,'結','tie; conclude','むすぶ','ケツ','むすぶ','action','U+7D50','r189,r040',12,4,'left-right',1),
    (720,'案','proposal; plan','あん','アン','','abstract','U+6848','r113,r040',10,4,'top-bottom',1),
    (721,'整','arrange; tidy','ととのえる','セイ','ととのえる','action','U+6574','r101,r065',16,3,'left-right',1),
    (722,'器','vessel; device','うつわ','キ','うつわ','object','U+5668','r037',15,4,'enclosure',1),
    (723,'健','healthy','けん','ケン','','health','U+5065','r012,r076',11,4,'left-right',1),
    (724,'標','mark; target','しるし','ヒョウ','しるし','abstract','U+6A19','r113,r040',15,4,'left-right',1),
    (725,'司','manage; officer','つかさ','シ','つかさ','society','U+53F8','r037',5,4,'enclosure',1),
    (726,'康','health; peace','こう','コウ','','health','U+5EB7','r072',11,4,'enclosure',1),
    (727,'級','class; grade','きゅう','キュウ','','education','U+7D1A','r189,r036',9,3,'left-right',1),
    (728,'救','rescue','すくう','キュウ','すくう','action','U+6551','r101,r065',11,4,'left-right',1),
    (729,'節','section; joint','ふし','セツ','ふし','abstract','U+7BC0','r118,r040',13,4,'top-bottom',1),
    (730,'泣','cry','なく','キュウ','なく','action','U+6CE3','r090,r176',8,4,'left-right',1),
    (731,'保','protect; keep','たもつ','ホ','たもつ','action','U+4FDD','r012,r009',9,5,'left-right',1),
    (732,'基','base; foundation','もとい','キ','もとい','abstract','U+57FA','r040,r037',11,5,'top-bottom',1),
    (733,'価','price; value','ね','カ','ね','money','U+4FA1','r012,r016',8,5,'left-right',1),
    (734,'応','respond','おうじる','オウ','おうじる','action','U+5FDC','r092',7,5,'top-bottom',1),
    (735,'検','examine; check','けん','ケン','','action','U+691C','r113,r165',12,5,'left-right',1),
    (736,'展','expand; display','てんじる','テン','てんじる','action','U+5C55','r058',10,6,'top-bottom',1),
    (737,'条','clause; strip','じょう','ジョウ','','abstract','U+6761','r075',7,5,'single',2),
    (738,'独','alone; Germany','ひとり','ドク','ひとり','abstract','U+72EC','r094',9,5,'left-right',2),
    (739,'率','rate; lead','りつ','ソツ,リツ','','abstract','U+7387','r069',11,5,'top-bottom',2),
    (740,'張','stretch; display','はる','チョウ','はる','action','U+5F35','r076,r046',11,5,'left-right',2),
    (741,'環','ring; environ','かん','カン','','abstract','U+74B0','r146,r040',17,5,'left-right',2),
    (742,'評','evaluate','ひょう','ヒョウ','','abstract','U+8A55','r228,r040',12,5,'left-right',2),
    (743,'製','manufacture','せい','セイ','','action','U+88FD','r183,r040',14,5,'top-bottom',2),
    (744,'授','grant; teach','さずける','ジュ','さずける','education','U+6388','r089,r040',11,5,'left-right',2),
    (745,'批','criticize','ひ','ヒ','','abstract','U+6279','r089,r040',7,6,'left-right',2),
    (746,'修','master; study','おさめる','シュウ,シュ','おさめる','education','U+4FEE','r012,r067',10,5,'left-right',2),
    (747,'拡','expand; enlarge','ひろげる','カク','ひろげる','action','U+62E1','r089,r040',8,6,'left-right',2),
    (748,'故','reason; late','ゆえ','コ','ゆえ','abstract','U+6545','r101,r065',9,5,'left-right',2),
    (749,'異','different','ことなる','イ','ことなる','abstract','U+7570','r037,r040',11,6,'top-bottom',2),
    (750,'善','good; virtue','よい','ゼン','よい','abstract','U+5584','r112,r037',12,6,'top-bottom',2),
    (751,'志','aspiration','こころざし','シ','こころざし','abstract','U+5FD7','r040,r092',7,5,'top-bottom',2),
    (752,'恵','blessing; grace','めぐむ','ケイ','めぐむ','abstract','U+6075','r092,r040',10,6,'top-bottom',2),
    (753,'賃','wages; rent','ちん','チン','','money','U+8CDB','r236,r040',13,6,'top-bottom',2),
    (754,'宙','midair; universe','ちゅう','チュウ','','abstract','U+5B99','r174,r037',8,6,'top-bottom',2),
    (755,'操','operate; conduct','あやつる','ソウ','あやつる','action','U+64CD','r089,r040',16,6,'left-right',2),
    (756,'壁','wall','かべ','ヘキ','かべ','object','U+58C1','r040,r076',16,6,'top-bottom',2),
    (757,'仮','temporary; suppose','かり','カ','かり','abstract','U+4EEE','r012,r029',6,5,'left-right',3),
    (758,'看','watch over','みまもる','カン','みまもる','action','U+770B','r097,r162',9,6,'top-bottom',3),
    (759,'較','compare','くらべる','カク','くらべる','abstract','U+6BD4','r245,r040',13,6,'left-right',3),
    (760,'狭','narrow','せまい','キョウ','せまい','description','U+72ED','r094,r037',9,6,'left-right',3),
    (761,'暖','warm','あたたかい','ダン','あたたかい','nature','U+6696','r107,r040',13,6,'left-right',3),
    (762,'氏','family name; Mr.','うじ','シ','うじ','people','U+6C0F','r083',4,4,'single',3),
    (763,'統','unify; govern','おさめる','トウ','おさめる','action','U+7D71','r189,r040',12,5,'left-right',3),
    (764,'派','faction; dispatch','は','ハ','','society','U+6D3E','r090,r040',9,6,'left-right',3),
    (765,'策','plan; policy','さく','サク','','abstract','U+7B56','r118,r040',12,6,'top-bottom',3),
    (766,'提','propose; lift','さげる','テイ','さげる','action','U+63D0','r089,r040',12,5,'left-right',3),
]

for d in n1_data:
    (suf, kanji, meaning, hiragana, on_yomi, kun_yomi, theme, unicode_val, radicals_raw, strokes, grade, comp, lesson) = d
    id_str = 'k{:04d}'.format(suf)
    radicals = clean_radicals(radicals_raw)
    row = [id_str, kanji, meaning, hiragana, on_yomi, kun_yomi,
           '', 'N1', theme, unicode_val, '', radicals,
           str(strokes), str(grade), comp, str(lesson)]
    rows.append(row)

# Write output
with open(OUT, 'w', encoding='utf-8', newline='') as f:
    writer = csv.writer(f, quoting=csv.QUOTE_MINIMAL)
    writer.writerow(HEADER)
    for row in rows:
        writer.writerow(row)

print(f"Done. Wrote {len(rows)} kanji rows.")
